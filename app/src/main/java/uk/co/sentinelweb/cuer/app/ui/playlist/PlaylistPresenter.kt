package uk.co.sentinelweb.cuer.app.ui.playlist

import com.roche.mdas.util.wrapper.ToastWrapper
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.Const
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
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
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder
) : PlaylistContract.Presenter, QueueMediatorContract.ProducerListener {


    override fun initialise() {
        initListCheck()
        queue.addProducerListener(this)
        queue.refreshQueue()
    }

    override fun loadList() {
        updateListContent(queue.getPlayList() ?: Const.EMPTY_PLAYLIST)
    }

    override fun refreshList() {
        queue.refreshQueue()
    }

    override fun setFocusId(videoId: String) {
        state.focusItemId = videoId
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
        // toastWrapper.showToast("left: ${item.topText}")
        getDomainPlaylistItem(item)?.run {
            queue.removeItem(this)
        }
    }

    override fun onItemClicked(item: PlaylistModel.PlaylistItemModel) {
        if (!(ytContextHolder.get()?.isConnected() ?: false)) {
            view.showAlert("Please connect to a chromecast")
            return
        }
        getDomainPlaylistItem(item)?.run {
            queue.onItemSelected(this)
        }
    }

    private fun getIndexByVideoId(videoId: String): Int? {
        return queue.getPlayList()
            ?.items
            ?.indexOfFirst { it.media.mediaId == videoId }
    }

    private fun getDomainPlaylistItem(item: PlaylistModel.PlaylistItemModel): PlaylistItemDomain? {
        return queue.getPlayList()
            ?.items
            ?.first { it.media.url == item.url }
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

        state.focusItemId?.let {
            getIndexByVideoId(it)?.apply {
                view.scrollToItem(this)
            }
        }
    }

}