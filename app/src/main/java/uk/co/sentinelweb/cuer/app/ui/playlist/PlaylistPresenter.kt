package uk.co.sentinelweb.cuer.app.ui.playlist

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.dialog.playlist.PlaylistSelectDialogModelCreator
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.CURRENT_PLAYLIST_ID
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.ext.indexOfItemId
import uk.co.sentinelweb.cuer.domain.ext.itemWitId
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator

class PlaylistPresenter(
    private val view: PlaylistContract.View,
    private val state: PlaylistState,
    private val repository: MediaDatabaseRepository,
    private val playlistRepository: PlaylistDatabaseRepository,
    private val modelMapper: PlaylistModelMapper,
    private val contextProvider: CoroutineContextProvider,
    private val queue: QueueMediatorContract.Producer,
    private val toastWrapper: ToastWrapper,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val ytJavaApi: YoutubeJavaApiWrapper,
    private val shareWrapper: ShareWrapper,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>,
    private val playlistMutator: PlaylistMutator,
    private val log: LogWrapper,
    private val playlistDialogModelCreator: PlaylistSelectDialogModelCreator,
    private val timeProvider: TimeProvider
) : PlaylistContract.Presenter, QueueMediatorContract.ProducerListener {

    private val isQueuedPlaylist: Boolean
        get() = state.playlist?.let { queue.playlist?.id == state.playlist?.id } ?: false

    private fun queueExecIf(
        block: QueueMediatorContract.Producer.() -> Unit,
        elseBlock: (QueueMediatorContract.Producer.() -> Unit)? = null
    ) {
        if (isQueuedPlaylist) block(queue)
        else elseBlock?.let { it(queue) }
    }

    private fun queueExecIf(block: QueueMediatorContract.Producer.() -> Unit) {
        if (isQueuedPlaylist) block(queue)
    }

    override fun initialise() {
        queue.addProducerListener(this)
        state.playlistId = prefsWrapper.getLong(CURRENT_PLAYLIST_ID)
    }

    override fun refreshList() {
        refreshPlaylist()
    }

    override fun setFocusMedia(mediaDomain: MediaDomain) {
        //state.addedMedia = mediaDomain
    }

    override fun destroy() {
        queue.removeProducerListener(this)
    }

    override fun onItemSwipeRight(item: PlaylistModel.PlaylistItemModel) {// move
        state.viewModelScope.launch {
            state.selectedPlaylistItem = state.playlist?.itemWitId(item.id)
            playlistDialogModelCreator.loadPlaylists { allPlaylists ->
                view.showPlaylistSelector(
                    playlistDialogModelCreator.mapPlaylistSelectionForDialog(
                        allPlaylists, setOf(state.playlist!!), false,
                        itemClick = { which: Int, _ ->
                            if (which < allPlaylists.size) {
                                allPlaylists[which].id?.let { moveItemToPlaylist(it) }
                            } else {
                                view.showPlaylistCreateDialog()
                            }
                        },
                        dismiss = { view.resetItemsState() }
                    )
                )
            }
        }
    }

    override fun onPlaylistSelected(playlist: PlaylistDomain) {
        playlist.id?.let { moveItemToPlaylist(it) }
    }

    private fun moveItemToPlaylist(it: Long) {
        state.selectedPlaylistItem?.let { moveItem ->
            state.viewModelScope.launch {
                moveItem.copy(playlistId = it, order = timeProvider.currentTimeMillis())
                    .apply { playlistRepository.savePlaylistItem(this) }
                executeRefresh()
            }
        }
    }

    override fun onItemSwipeLeft(item: PlaylistModel.PlaylistItemModel) {
        state.viewModelScope.launch {
            delay(400)
            state.playlist?.items?.apply {
                find { it.id == item.id }?.let { deleteItem ->
                    state.deletedPlaylistItem = deleteItem
                    playlistRepository.delete(deleteItem)
                    queueExecIf { itemRemoved(deleteItem) }
                    // todo check if media is on other playlist and delete if not?
                    //repository.delete(deleteItem.media)
                    view.showDeleteUndo("Deleted: ${deleteItem.media.title}")
                    state.focusIndex = indexOf(deleteItem)
                    executeRefresh(false)
                }
            }
        }
    }

    override fun onItemClicked(item: PlaylistModel.PlaylistItemModel) {
        state.playlist?.itemWitId(item.id)?.let { itemDomain ->
            if (!(ytContextHolder.isConnected())) {
                toastWrapper.show("No chromecast -> playing locally")
                view.playLocal(itemDomain.media)
            } else {
                if (isQueuedPlaylist) {
                    queue.onItemSelected(itemDomain)
                } else {
                    view.showAlertDialog(modelMapper.mapChangePlaylistAlert({
                        state.playlist?.let {
                            prefsWrapper.putLong(CURRENT_PLAYLIST_ID, it.id!!)
                            queue.refreshQueueFrom(it)
                            queue.onItemSelected(itemDomain, true)
                        }
                    }))
                }
            }
        } // todo error
    }

    override fun scroll(direction: PlaylistContract.ScrollDirection) {
        view.scrollTo(direction)
    }

    override fun onItemPlay(item: PlaylistModel.PlaylistItemModel, external: Boolean) {
        state.playlist?.itemWitId(item.id)?.let { itemDomain ->
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
        state.playlist?.itemWitId(item.id)?.let { itemDomain ->
            if (!ytJavaApi.launchChannel(itemDomain.media)) {
                toastWrapper.show("can't launch channel")
            }
        } ?: toastWrapper.show("can't find video")
    }

    override fun onItemStar(item: PlaylistModel.PlaylistItemModel) {
        toastWrapper.show("todo: star ${item.id}")
    }

    override fun onItemShare(item: PlaylistModel.PlaylistItemModel) {
        state.playlist?.itemWitId(item.id)?.let { itemDomain ->
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
                    queueExecIf {
                        refreshQueueFrom(plist)
                        view.highlightPlayingItem(currentItemIndex)
                    }
                }
            }
        } else {
            //toastWrapper.show("Move failed ..")
            log.d("commitMove: Move failed .. ")
            refreshPlaylist()
        }
        state.dragFrom = null
        state.dragTo = null
    }

    override fun setPlaylistData(plId: Long?, plItemId: Long?, playNow: Boolean) {
        state.viewModelScope.launch {
            plId?.apply {
                state.playlistId = plId
                executeRefresh()

                if (playNow) {
                    state.playlist?.apply {
                        queue.playNow(this, plItemId)// todo current item isnt set properly in queue
                        state.playlist = queue.playlist?.copy()
                    }
                } else {
                    state.playlist?.apply {
                        indexOfItemId(plItemId)?.also { foundIndex ->
                            view.scrollToItem(foundIndex)
                        }
                    }
                }
                queueExecIf {
                    view.highlightPlayingItem(queue.currentItemIndex)
                    currentItemIndex?.apply { view.scrollToItem(this) }
                }
            } ?: run {
                executeRefresh()
            }
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

    override fun onItemChanged() {
        queueExecIf {
            currentItemIndex?.apply {
                state.playlist = state.playlist?.copy(currentIndex = this)
            } ?: throw IllegalStateException()
            view.highlightPlayingItem(currentItemIndex)
            currentItemIndex?.apply { view.scrollToItem(this) }
        }
    }

    private fun refreshPlaylist() {
        state.viewModelScope.launch { executeRefresh() }
    }

    private suspend fun executeRefresh(animate: Boolean = true) {
        try {
            (state.playlistId
                ?.let { playlistRepository.load(it, flat = false) }
                ?.takeIf { it.isSuccessful }
                ?.data
                ?: run {
                    playlistRepository.loadList(PlaylistDatabaseRepository.DefaultFilter(flat = false))
                        .takeIf { it.isSuccessful && it.data?.size ?: 0 > 0 }
                        ?.data?.get(0)
                })
                .also { state.playlist = it }
                ?.also {
                    queueExecIf {
                        refreshQueueFrom(it)
                        view.highlightPlayingItem(currentItemIndex)
                    }
                }
                .also { view.setSubTitle(state.playlist?.title ?: "No playlist" + (if (isQueuedPlaylist) " - playing" else "")) }
                ?.let { modelMapper.map(it) }
                ?.also { view.setList(it.items, animate) }
                .also {
                    state.focusIndex?.apply {
                        view.scrollToItem(this)
                        state.lastFocusIndex = state.focusIndex
                        state.focusIndex = null
                    }
                }
        } catch (e: Throwable) {
            log.e("Error loading playlist", e)
        }
    }
}