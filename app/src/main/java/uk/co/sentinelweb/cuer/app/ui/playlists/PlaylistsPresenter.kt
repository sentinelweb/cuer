package uk.co.sentinelweb.cuer.app.ui.playlists

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemModel
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.CURRENT_PLAYLIST_ID
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistsPresenter(
    private val view: PlaylistsContract.View,
    private val state: PlaylistsState,
    private val repository: MediaDatabaseRepository,
    private val playlistRepository: PlaylistDatabaseRepository,
    private val modelMapper: PlaylistsModelMapper,
    private val contextProvider: CoroutineContextProvider,
    private val log: LogWrapper,
    private val toastWrapper: ToastWrapper,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>
) : PlaylistsContract.Presenter, QueueMediatorContract.ProducerListener {

    override fun initialise() {

    }

    override fun refreshList() {
        refreshPlaylist()
    }

    override fun setFocusMedia(mediaDomain: MediaDomain) {
        //state.addedMedia = mediaDomain
    }

    override fun destroy() {
    }

    override fun onItemSwipeRight(item: ItemModel) {
        view.gotoEdit(item.id)
    }

    override fun onItemSwipeLeft(item: ItemModel) {
        state.viewModelScope.launch {
            delay(400)
            state.playlists.apply {
                find { it.id == item.id }?.let { playlist ->
                    state.deletedPlaylist = playlist
                    playlistRepository.delete(playlist)
                    // todo check if media is on other playlist and delete if not?
                    //repository.delete(deleteItem.media)
                    view.showDeleteUndo("Deleted playlist: ${playlist.title}")
                    //state.focusIndex = indexOf(playlist)
                    executeRefresh(false)
                }
            }
        }
    }

    override fun onItemClicked(item: ItemModel) {
        view.gotoPlaylist(item.id, false)
    }

    override fun onItemPlay(item: ItemModel, external: Boolean) {
        view.gotoPlaylist(item.id, true)
    }

    override fun onItemStar(item: ItemModel) {
        toastWrapper.show("todo: star ${item.id}")
    }

    override fun onItemShare(item: ItemModel) {
        toastWrapper.show("share: ${item.title}")
    }

    override fun moveItem(fromPosition: Int, toPosition: Int) {
        if (state.dragFrom == null) {
            state.dragFrom = fromPosition
        }
        state.dragTo = toPosition
    }

    override fun commitMove() {
        if (state.dragFrom != null && state.dragTo != null) {
            toastWrapper.show("todo save move ..")
        } else {
            toastWrapper.show("Move failed ..")
            refreshPlaylist()
        }
        state.dragFrom = null
        state.dragTo = null
    }

    override fun undoDelete() {
        state.deletedPlaylist?.let { itemDomain ->
            state.viewModelScope.launch {
                playlistRepository.save(itemDomain)
                state.deletedPlaylist = null
                executeRefresh(false)
            }
        }
    }

    override fun onPlaylistUpdated(list: PlaylistDomain) {
        refreshPlaylist()
    }

    override fun onItemChanged() {

    }

    private fun refreshPlaylist() {
        state.viewModelScope.launch { executeRefresh(false) }
    }

    private suspend fun executeRefresh(focusCurent: Boolean) {
        state.playlists = playlistRepository.loadList(null)
            .takeIf { it.isSuccessful }
            ?.data
            ?: listOf()

        state.playlists
            .let { modelMapper.map(it) }
            .also { view.setList(it.items) }
            .takeIf { focusCurent }
            ?.let {
                state.playlists.apply {
                    prefsWrapper.getLong(CURRENT_PLAYLIST_ID)
                        ?.let { focusId -> view.scrollToItem(indexOf(find { it.id == focusId })) }
                }
            }
    }

    override fun onResume() {
        state.viewModelScope.launch { executeRefresh(true) }
    }
}