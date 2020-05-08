package uk.co.sentinelweb.cuer.app.ui.playlist

import com.roche.mdas.util.wrapper.ToastWrapper
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.Const
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.wrapper.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain.VIDEO
import uk.co.sentinelweb.cuer.domain.MediaDomain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeVideosInteractor
import uk.co.sentinelweb.cuer.ui.queue.dummy.Queue

class PlaylistPresenter(
    private val view: PlaylistContract.View,
    private val state: PlaylistState,
    private val repository: MediaDatabaseRepository,
    private val modelMapper: PlaylistModelMapper,
    private val contextProvider: CoroutineContextProvider,
    private val queue: QueueMediatorContract.Mediator,
    private val toastWrapper: ToastWrapper,
    private val ytInteractor: YoutubeVideosInteractor,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val ytJavaApi: YoutubeJavaApiWrapper,
    private val shareWrapper: ShareWrapper

) : PlaylistContract.Presenter, QueueMediatorContract.ProducerListener {

    override fun initialise() {
        initListCheck()
        queue.addProducerListener(this)
        queue.refreshQueue()
    }

    override fun loadList() {
        updateListContent(queue.getPlaylist() ?: Const.EMPTY_PLAYLIST)
    }

    override fun refreshList() {
        queue.refreshQueue()
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
        getDomainPlaylistItem(item.url)?.run {
            queue.removeItem(this)
        }
    }

    override fun onItemClicked(item: PlaylistModel.PlaylistItemModel) {
        getDomainPlaylistItem(item.url)?.run {
            if (!(ytContextHolder.get()?.isConnected() ?: false)) {
                toastWrapper.show("No chromecast -> playing locally")
                view.playLocal(this.media)
            } else {
                queue.onItemSelected(this)
            }
        }
    }

    override fun onItemPlay(item: PlaylistModel.PlaylistItemModel, external: Boolean) {
        if (external) {
            if (ytJavaApi.canLaunchVideo()) {
                getDomainPlaylistItem(item.url)?.run {
                    ytJavaApi.launchVideo(this.media)
                } ?: toastWrapper.show("can't find video")
            } else {
                toastWrapper.show("can't launch video")
            }
        } else {
            getDomainPlaylistItem(item.url)?.run {
                view.playLocal(this.media)
            }
        }
    }

    override fun onItemShowChannel(item: PlaylistModel.PlaylistItemModel) {
        if (ytJavaApi.canLaunchChannel()) {
            getDomainPlaylistItem(item.url)?.run {
                ytJavaApi.launchChannel(this.media)
            } ?: toastWrapper.show("can't find video")
        } else {
            toastWrapper.show("can't launch channel")
        }
    }

    override fun onItemStar(item: PlaylistModel.PlaylistItemModel) {
        toastWrapper.show("todo: star ${item.id}")
    }

    override fun onItemShare(item: PlaylistModel.PlaylistItemModel) {
        getDomainPlaylistItem(item.url)?.run {
            shareWrapper.share(this.media)
        }
    }

    override fun moveItem(fromPosition: Int, toPosition: Int) {
        queue.moveItem(fromPosition, toPosition)
    }

    override fun playNow(mediaDomain: MediaDomain) {
        if (!(ytContextHolder.get()?.isConnected() ?: false)) {
            toastWrapper.show("No chromecast -> playing locally")
            view.playLocal(mediaDomain)
        } else {
            getDomainPlaylistItem(mediaDomain.url)?.let {
                queue.onItemSelected(it)
            } ?: run {
                state.playAddedAfterRefresh = true
            }
        }
    }

    private fun getIndexByVideoId(videoId: String): Int? {
        return queue.getPlaylist()
            ?.items
            ?.indexOfFirst { it.media.mediaId == videoId }
    }

    private fun getDomainPlaylistItem(url: String): PlaylistItemDomain? {
        return queue.getPlaylist()
            ?.items
            ?.firstOrNull() { it.media.url == url }
    }

    private fun initListCheck() {
        state.jobs.add(contextProvider.MainScope.launch {
            val count = repository.count()
            if (count == 0) {
                Queue.ITEMS
                    .map { mapQueueToMedia(it) }
                    .map { it.mediaId }
                    .let { ytInteractor.videos(it) }
                    .also { repository.save(it) }
                    .also { loadList() }
            }
        })
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
        list.items
            .map { modelMapper.map(it) }
            .also { view.setList(it) }

        state.addedMedia?.let { added ->
            if (state.playAddedAfterRefresh) {
                getDomainPlaylistItem(added.url)?.let {
                    queue.onItemSelected(it)
                    state.addedMedia = null
                    state.playAddedAfterRefresh = false
                }
            }
            getIndexByVideoId(added.mediaId)?.apply {
                view.scrollToItem(this)
                state.addedMedia = null
            }

        }
    }

}