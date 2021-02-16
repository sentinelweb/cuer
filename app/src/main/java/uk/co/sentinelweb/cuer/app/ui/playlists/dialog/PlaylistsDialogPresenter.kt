package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class PlaylistsDialogPresenter(
    private val view: PlaylistsDialogContract.View,
    private val state: PlaylistsDialogContract.State,
    private val playlistRepository: PlaylistDatabaseRepository,
    private val modelMapper: PlaylistsModelMapper,
    private val log: LogWrapper,
    private val toastWrapper: ToastWrapper,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>,
    private val coroutines: CoroutineContextProvider
) : PlaylistsDialogContract.Presenter {

    init {
        log.tag(this)
    }

    override fun refreshList() {
        refreshPlaylists()
    }

    override fun destroy() {
    }

    override fun onItemSwipeRight(item: ItemContract.Model) {
        refreshPlaylists()
    }

    override fun onItemSwipeLeft(item: ItemContract.Model) {
        refreshPlaylists()
    }

    override fun onItemClicked(item: ItemContract.Model) {
        state.playlists.indexOfFirst { it.id == item.id }
            .takeIf { it > -1 }
            ?.apply { state.config.model.itemClick(this, true) }
            ?.also { view.dismiss() }
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
            refreshPlaylists()
        }
        state.dragFrom = null
        state.dragTo = null
    }

    private fun refreshPlaylists() {
        state.viewModelScope.launch { executeRefresh() }
    }

    private suspend fun executeRefresh(animate: Boolean = true) {
        state.playlists = playlistRepository.loadList(null)
            .takeIf { it.isSuccessful }
            ?.data
            ?: listOf()

        state.playlistStats = playlistRepository
            .loadPlaylistStatList(state.playlists.mapNotNull { it.id }).data
            ?: listOf()

        state.playlists
            .associateWith { pl -> state.playlistStats.find { it.playlistId == pl.id } }
            .let { modelMapper.map(it, null, false) }
            .takeIf { coroutines.mainScopeActive }
            ?.also { view.setList(it, animate) }

    }

    override fun setConfig(config: PlaylistsDialogContract.Config) {
        state.config = config
    }

    override fun onAddPlaylist() {
        (state.playlists.size + 1)
            .apply { state.config.model.itemClick(this, true) }
            .also { view.dismiss() }
    }

    override fun onResume() {
        state.viewModelScope.launch { executeRefresh(true) }
        playlistRepository.updates
            .onEach { refreshPlaylists() }
            .let { coroutines.mainScope }
    }

    override fun onPause() {
        coroutines.cancel()
    }
}