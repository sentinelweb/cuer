package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.ChannelPlatformIdFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Options
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.LAST_PLAYLIST_ADDED_TO
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.LAST_PLAYLIST_CREATED
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class PlaylistsDialogPresenter(
    private val view: PlaylistsDialogContract.View,
    private val state: PlaylistsDialogContract.State,
    private val playlistRepository: PlaylistDatabaseRepository,
    private val playlistOrchestrator: PlaylistOrchestrator,
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

    override fun onItemClicked(item: ItemContract.Model) {
        state.playlists.find { it.id == item.id }
            ?.apply { state.config.itemClick(this, true) }
            ?.also { view.dismiss() }
    }

    private fun refreshPlaylists() {
        state.viewModelScope.launch { executeRefresh() }
    }

    private suspend fun executeRefresh(animate: Boolean = true) {
        if (!state.channelSearchApplied) {
            state.config.suggestionsMedia?.apply {
                playlistOrchestrator.loadList(ChannelPlatformIdFilter(this.channelData.platformId!!), Options(LOCAL))
                    .apply { state.priorityPlaylistIds.addAll(this.map { it.id!! }) }
            }
            state.channelSearchApplied = true
        }
        state.playlists = playlistOrchestrator.loadList(OrchestratorContract.AllFilter(), Options(LOCAL))
            .sortedWith(compareBy({ !state.priorityPlaylistIds.contains(it.id) }, { !it.starred }, { it.title.toLowerCase() }))

        state.playlistStats = playlistRepository
            .loadPlaylistStatList(state.playlists.mapNotNull { it.id }).data
            ?: listOf()

        state.playlists
            .associateWith { pl -> state.playlistStats.find { it.playlistId == pl.id } }
            .let { modelMapper.map(it, null, false) }
            .takeIf { coroutines.mainScopeActive }
            ?.also { view.setList(it, animate) }

    }

    override fun onDismiss() {
        state.config.dismiss()
    }

    override fun setConfig(config: PlaylistsDialogContract.Config) {
        state.config = config
        prefsWrapper.getLong(LAST_PLAYLIST_ADDED_TO)
            ?.apply { state.priorityPlaylistIds.add(this) }
        prefsWrapper.getLong(LAST_PLAYLIST_CREATED)
            ?.takeIf { !state.priorityPlaylistIds.contains(it) }
            ?.apply { state.priorityPlaylistIds.add(this) }
    }

    override fun onAddPlaylist() {
        (state.playlists.size + 1)
            .apply { state.config.itemClick(null, true) }
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