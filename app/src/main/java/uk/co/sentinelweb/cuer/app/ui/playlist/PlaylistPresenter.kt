package uk.co.sentinelweb.cuer.app.ui.playlist

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain.VIDEO
import uk.co.sentinelweb.cuer.domain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.ui.queue.dummy.Queue

class PlaylistPresenter(
    private val view: PlaylistContract.View,
    private val state: PlaylistState,
    private val repository: MediaDatabaseRepository,
    private val modelMapper: PlaylistModelMapper,
    private val contextProvider: CoroutineContextProvider,
    private val queue: QueueMediatorContract.Mediator,
    private val toastWrapper: ToastWrapper,
    private val ytInteractor: YoutubeInteractor,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val ytJavaApi: YoutubeJavaApiWrapper,
    private val shareWrapper: ShareWrapper

) : PlaylistContract.Presenter, QueueMediatorContract.ProducerListener {

    override fun initialise() {
        initListCheck()
        queue.addProducerListener(this)
    }

    override fun loadList() {
        queue.refreshQueueBackground()
    }

    override fun refreshList() {
        queue.refreshQueueBackground()
    }

    override fun setFocusMedia(mediaDomain: MediaDomain) {
        state.addedMedia = mediaDomain
    }

    override fun destroy() {
        state.jobs.forEach { it.cancel() }
        state.jobs.clear()
        queue.removeProducerListener(this)
    }

    override fun onItemSwipeRight(item: PlaylistModel.PlaylistItemModel) {
        toastWrapper.show("right: ${item.topText}")
    }

    override fun onItemSwipeLeft(item: PlaylistModel.PlaylistItemModel) {
        state.jobs.add(contextProvider.MainScope.launch {
            delay(400)
            queue.getItemFor(item.url)?.run {
                state.deletedMedia = this.media
                state.focusIndex = queue.itemIndex(this)
                queue.removeItem(this)
                view.showDeleteUndo("Deleted: ${media.title}")
            }
        })
    }

    override fun onItemClicked(item: PlaylistModel.PlaylistItemModel) {
        queue.getItemFor(item.url)?.run {
            if (!(ytContextHolder.isConnected())) {
                toastWrapper.show("No chromecast -> playing locally")
                view.playLocal(this.media)
            } else {
                queue.onItemSelected(this)
            }
        }
    }

    override fun scroll(direction: PlaylistContract.ScrollDirection) {
        view.scrollTo(direction)
    }

    override fun onItemPlay(item: PlaylistModel.PlaylistItemModel, external: Boolean) {
        if (external) {
            queue.getItemFor(item.url)?.run {
                if (!ytJavaApi.launchVideo(this.media)) {
                    toastWrapper.show("can't launch video")
                }
            } ?: toastWrapper.show("can't find video")
        } else {
            queue.getItemFor(item.url)?.run {
                view.playLocal(this.media)
            }
        }
    }

    override fun onItemShowChannel(item: PlaylistModel.PlaylistItemModel) {
        queue.getItemFor(item.url)?.run {
            if (!ytJavaApi.launchChannel(this.media)) {
                toastWrapper.show("can't launch channel")
            }
        } ?: toastWrapper.show("can't find video")
    }

    override fun onItemStar(item: PlaylistModel.PlaylistItemModel) {
        toastWrapper.show("todo: star ${item.id}")
    }

    override fun onItemShare(item: PlaylistModel.PlaylistItemModel) {
        queue.getItemFor(item.url)?.run {
            shareWrapper.share(this.media)
        }
    }

    override fun moveItem(fromPosition: Int, toPosition: Int) {
        queue.moveItem(fromPosition, toPosition)
    }

    override fun playNow(mediaDomain: MediaDomain) {
        if (!(ytContextHolder.isConnected())) {
            toastWrapper.show("No chromecast -> playing locally")
            view.playLocal(mediaDomain)
        } else {
            queue.getItemFor(mediaDomain.url)?.let {
                queue.onItemSelected(it)
            } ?: run {
                state.playAddedAfterRefresh = true
                queue.refreshQueueBackground() // In this case the ques isn't refeshed in share as it wasn't added
            }
        }
    }

    private fun initListCheck() {
        state.jobs.add(contextProvider.MainScope.launch {
            repository.count()
                .takeIf { it.isSuccessful && it.data == 0 }
                ?.let { Queue.ITEMS }
                ?.map { mapQueueToMedia(it) }
                ?.map { it.mediaId }
                ?.let { ytInteractor.videos(it) }
                ?.takeIf { it.isSuccessful }
                ?.also { it.data?.let { repository.save(it) } }
                .also { loadList() }
        })
    }

    override fun undoDelete() {
        state.deletedMedia?.run {
            state.jobs.add(contextProvider.MainScope.launch {
                repository.save(this@run)
                state.focusIndex = state.lastFocusIndex
                queue.refreshQueue()
                state.deletedMedia = null
            })
        }
    }

    private fun mapQueueToMedia(it: Queue.QueueItem): MediaDomain {
        return MediaDomain(
            url = it.url,
            mediaId = it.getId(),
            title = it.title,
            platform = YOUTUBE,
            description = null,
            dateLastPlayed = null,
            duration = null,
            mediaType = VIDEO,
            id = null,
            positon = null
        )
    }

    override fun onPlaylistUpdated(list: PlaylistDomain) {
        updateListContent(list)
    }

    private fun updateListContent(list: PlaylistDomain) {
        list
            .let { modelMapper.map(it) }
            .also { view.setList(it.items) }
            .also {
                state.focusIndex?.apply {
                    view.scrollToItem(this)
                    state.lastFocusIndex = state.focusIndex
                    state.focusIndex = null
                } ?: state.addedMedia?.let { added ->
                    queue.getItemFor(added.url)?.let {
                        view.scrollToItem(queue.itemIndex(it)!!)
                        if (state.playAddedAfterRefresh) {
                            queue.onItemSelected(it)
                            state.playAddedAfterRefresh = false
                        }
                        state.addedMedia = null
                    }
                } ?: run {
                    view.scrollToItem(
                        if (list.currentIndex > -1) list.currentIndex else list.items.size - 1
                    )
                }
            }
    }
}