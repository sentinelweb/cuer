package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistStatsOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.LAST_PLAYLIST_ADDED_TO
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.LAST_PLAYLIST_CREATED
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ext.buildLookup
import uk.co.sentinelweb.cuer.domain.ext.buildTree
import java.util.Locale.getDefault

class PlaylistsDialogPresenter(
    private val view: PlaylistsDialogContract.View,
    private val state: PlaylistsDialogContract.State,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistStatsOrchestrator: PlaylistStatsOrchestrator,
    private val modelMapper: PlaylistsModelMapper,
    private val dialogModelMapper: PlaylistsDialogModelMapper,
    private val log: LogWrapper,
    private val toastWrapper: ToastWrapper,
    private val prefsWrapper: GeneralPreferencesWrapper,
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
            ?.apply {
                if (state.pinWhenSelected) {
                    prefsWrapper.putLong(GeneralPreferences.PINNED_PLAYLIST, id!!)
                }
            }
            ?.apply { state.config.itemClick(this, true) }
            ?.also { view.dismiss() }
    }

    private fun refreshPlaylists() {
        coroutines.mainScope.launch { executeRefresh() }
    }

    private suspend fun executeRefresh(animate: Boolean = false) {
        dialogModelMapper.map(state.playlistsModel, state.config, state.pinWhenSelected)
        if (!state.channelSearchApplied) {
            state.config.suggestionsMedia?.apply {
                playlistOrchestrator.loadList(ChannelPlatformIdFilter(this.channelData.platformId!!), LOCAL.flatOptions())
                    .apply { state.priorityPlaylistIds.addAll(this.map { it.id!! }) }
            }
            state.channelSearchApplied = true
        }

        val pinnedId = prefsWrapper.getLong(GeneralPreferences.PINNED_PLAYLIST)
        state.playlists = playlistOrchestrator.loadList(AllFilter(), LOCAL.flatOptions())
            .filter { it.config.editableItems }
            .sortedWith(compareBy(
                { it.id != pinnedId },
                { !state.priorityPlaylistIds.contains(it.id) },
                { !it.starred },
                { it.title.toLowerCase(getDefault()) })
            )
            .apply { state.treeRoot = buildTree() }
            .apply { state.treeLookup = state.treeRoot.buildLookup() }

        state.playlistStats = playlistStatsOrchestrator
            .loadList(IdListFilter(state.playlists.mapNotNull { it.id }), LOCAL.flatOptions())

        state.playlists
            .associateWith { pl -> state.playlistStats.find { it.playlistId == pl.id } }
            .let {
                state.playlistsModel = modelMapper.map(it, null,  false, pinnedId, null, state.treeLookup)
                dialogModelMapper.map(state.playlistsModel, state.config, state.pinWhenSelected)
            }
            .takeIf { coroutines.mainScopeActive }
            ?.also { view.setList(it, animate) }
    }

    private fun updateDialogModel() {
        dialogModelMapper.map(state.playlistsModel, state.config, state.pinWhenSelected)
            .takeIf { coroutines.mainScopeActive }
            ?.also { view.updateDialogModel(it) }
    }

    override fun onPinSelectedPlaylist(b: Boolean) {
        state.pinWhenSelected = b
        updateDialogModel()
    }

    override fun onDismiss() {
        state.config.dismiss()
        view.dismiss()
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
        coroutines.mainScope.launch { executeRefresh() }
        playlistOrchestrator.updates
            .onEach { refreshPlaylists() }
            .let { coroutines.mainScope }
    }

    override fun onPause() {
        coroutines.cancel()
    }
}