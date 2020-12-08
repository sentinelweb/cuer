package uk.co.sentinelweb.cuer.app.ui.playlist

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.SELECTED_PLAYLIST_ID
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator

class PlaylistPresenter(
    private val view: PlaylistContract.View,
    private val state: PlaylistState,
    private val repository: MediaDatabaseRepository,
    private val playlistRepository: PlaylistDatabaseRepository,
    private val modelMapper: PlaylistModelMapper,
    private val contextProvider: CoroutineContextProvider,
    //private val queue: QueueMediatorContract.Mediator,
    private val toastWrapper: ToastWrapper,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val ytJavaApi: YoutubeJavaApiWrapper,
    private val shareWrapper: ShareWrapper,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>,
    private val playlistMutator: PlaylistMutator,
    private val log: LogWrapper
) : PlaylistContract.Presenter, QueueMediatorContract.ProducerListener {

    override fun initialise() {
        //refreshPlaylist()
    }

    override fun refreshList() {
        refreshPlaylist()
    }

    override fun setFocusMedia(mediaDomain: MediaDomain) {
        //state.addedMedia = mediaDomain
    }

    override fun destroy() {
//        state.jobs.forEach { it.cancel() }
//        state.jobs.clear()
        //queue.removeProducerListener(this)
    }

    override fun onItemSwipeRight(item: PlaylistModel.PlaylistItemModel) {
        toastWrapper.show("right: ${item.topText}")
    }

    override fun onItemSwipeLeft(item: PlaylistModel.PlaylistItemModel) {
        state.viewModelScope.launch {
            delay(400)
            state.playlist?.items?.apply {
                find { it.id == item.id }?.let { deleteItem ->
                    state.deletedPlaylistItem = deleteItem
                    playlistRepository.delete(deleteItem)
                    // todo check if media is on other playlist and delete if not?
                    //repository.delete(deleteItem.media)
                    view.showDeleteUndo("Deleted: ${deleteItem.media.title}")
                    state.focusIndex = indexOf(deleteItem)
                    executeRefresh()
                }
            }
        }
    }

    override fun onItemClicked(item: PlaylistModel.PlaylistItemModel) {
        state.playlist?.items?.find { it.id == item.id }?.let { itemDomain ->
            if (!(ytContextHolder.isConnected())) {
                toastWrapper.show("No chromecast -> playing locally")
                view.playLocal(itemDomain.media)
            } else {
//                queue.onItemSelected(this)
            }
        }
    }

    override fun scroll(direction: PlaylistContract.ScrollDirection) {
        view.scrollTo(direction)
    }

    override fun onItemPlay(item: PlaylistModel.PlaylistItemModel, external: Boolean) {
        state.playlist?.items?.find { it.id == item.id }?.let { itemDomain ->
            if (external) {
                if (!ytJavaApi.launchVideo(itemDomain.media)) {
                    toastWrapper.show("can't launch video")
                }
            } else {
                view.playLocal(itemDomain.media)
            }
        } ?: toastWrapper.show("can't find video")
    }

    override fun onItemShowChannel(item: PlaylistModel.PlaylistItemModel) {
        state.playlist?.items?.find { it.id == item.id }?.let { itemDomain ->
            if (!ytJavaApi.launchChannel(itemDomain.media)) {
                toastWrapper.show("can't launch channel")
            }
        } ?: toastWrapper.show("can't find video")
    }

    override fun onItemStar(item: PlaylistModel.PlaylistItemModel) {
        toastWrapper.show("todo: star ${item.id}")
    }

    override fun onItemShare(item: PlaylistModel.PlaylistItemModel) {
        state.playlist?.items?.find { it.id == item.id }?.let { itemDomain ->
            shareWrapper.share(itemDomain.media)
        }
    }

    override fun moveItem(fromPosition: Int, toPosition: Int) {
        if (state.dragFrom == null) {
            state.dragFrom = fromPosition
        }
        state.dragTo = toPosition
    }

    override fun commitMove() {
        if (state.dragFrom != null && state.dragTo != null) {
            state.playlist = state.playlist?.let { playlist ->
                playlistMutator.moveItem(playlist, state.dragFrom!!, state.dragTo!!)
            }?.also { plist ->
                plist.let { modelMapper.map(it) }
                    .also { view.setList(it.items, false) }
            }?.also { plist ->
                state.viewModelScope.launch {
                    playlistRepository.save(plist, false)
                        .takeIf { it.isSuccessful }
                        ?: toastWrapper.show("Couldn't save playlist")
                    // todo fire que update
                    // queue.moveItem(fromPosition, toPosition)
                }
            }
        } else {
            toastWrapper.show("Move failed ..")
            refreshPlaylist()
        }
        state.dragFrom = null
        state.dragTo = null
    }

    override fun setPlaylist(it: Long) {
        prefsWrapper.putLong(SELECTED_PLAYLIST_ID, it)
        refreshPlaylist()
    }

    override fun playNow(mediaDomain: MediaDomain) {
        if (!(ytContextHolder.isConnected())) {
            toastWrapper.show("No chromecast -> playing locally")
            view.playLocal(mediaDomain)
        } else {
//            queue.getItemFor(mediaDomain.url)?.let {
//                queue.onItemSelected(it)
//            } ?: run {
//                state.playAddedAfterRefresh = true
//                queue.refreshQueueBackground() // In this case the ques isn't refeshed in share as it wasn't added
//            }
        }
    }

    override fun undoDelete() {
        state.deletedPlaylistItem?.let { itemDomain ->
            state.viewModelScope.launch {
                playlistRepository.savePlaylistItem(itemDomain)
                state.focusIndex = state.lastFocusIndex
                state.deletedPlaylistItem = null
                executeRefresh()
            }
        }
    }

    override fun onPlaylistUpdated(list: PlaylistDomain) {
        refreshPlaylist()
    }

    private fun refreshPlaylist() {
        state.viewModelScope.launch { executeRefresh() }
    }

    private suspend fun executeRefresh() {
        state.playlist =
            (prefsWrapper.getLong(SELECTED_PLAYLIST_ID)
                ?.let { playlistRepository.load(it, flat = false) }
                ?.takeIf { it.isSuccessful }
                ?.data
                ?: run {
                    playlistRepository.loadList(PlaylistDatabaseRepository.DefaultFilter(flat = false))
                        .takeIf { it.isSuccessful && it.data?.size ?: 0 > 0 }
                        ?.data?.get(0)
                })
//        state.playlist?.scanOrder().apply { log.d(this.toString()) }

        state.playlist
            ?.let { modelMapper.map(it) }
            ?.also { view.setList(it.items) }
            .also {
                state.focusIndex?.apply {
                    view.scrollToItem(this)
                    state.lastFocusIndex = state.focusIndex
                    state.focusIndex = null
                }
            }
    }

//    private fun updateListContent(list: PlaylistDomain) {
//        list
//            .let { modelMapper.map(it) }
//            .also { view.setList(it.items) }
//            .also {
//                state.focusIndex?.apply {
//                    view.scrollToItem(this)
//                    state.lastFocusIndex = state.focusIndex
//                    state.focusIndex = null
//                } ?: state.addedMedia?.let { added ->
//                    queue.getItemFor(added.url)?.let {
//                        view.scrollToItem(queue.itemIndex(it)!!)
//                        if (state.playAddedAfterRefresh) {
//                            queue.onItemSelected(it)
//                            state.playAddedAfterRefresh = false
//                        }
//                        state.addedMedia = null
//                    }
//                } ?: run {
//                    view.scrollToItem(
//                        if (list.currentIndex > -1) list.currentIndex else list.items.size - 1
//                    )
//                }
//            }
//    }
}