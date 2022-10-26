package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistStatsOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract.Companion.ADD_PLAYLIST_DUMMY
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract.Companion.ROOT_PLAYLIST_DUMMY
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ext.buildTree
import uk.co.sentinelweb.cuer.domain.ext.sort
import java.lang.Math.min

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
    private val coroutines: CoroutineContextProvider,
    private val recentLocalPlaylists: RecentLocalPlaylists,
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
        val findId = if (item.id >= 0) item.id else null // find top level node
        findId
            ?.let { state.playlists.find { it.id == findId } }
            ?.apply {
                if (state.pinWhenSelected) {
                    prefsWrapper.putLong(GeneralPreferences.PINNED_PLAYLIST, id!!)
                }
            }
            ?.apply { state.config.itemClick(this, true) }
            ?.also { view.dismiss() }
            ?: apply { state.config.itemClick(ROOT_PLAYLIST_DUMMY, true) }
                .also { view.dismiss() }
    }

    private fun refreshPlaylists() {
        coroutines.mainScope.launch { executeRefresh() }
    }

    private suspend fun executeRefresh(animate: Boolean = false) {
        state.channelPlaylistIds.clear()
        state.config.suggestionsMedia?.apply {
            playlistOrchestrator.loadList(
                ChannelPlatformIdFilter(this.channelData.platformId!!),
                LOCAL.flatOptions()
            )
                .apply { state.channelPlaylistIds.addAll(this.map { it.id!! }) }
        }

        val pinnedId = prefsWrapper.getLong(GeneralPreferences.PINNED_PLAYLIST)
        state.playlists = playlistOrchestrator.loadList(AllFilter(), LOCAL.flatOptions())
            .filter { it.config.editableItems }
            .apply { state.treeRoot = buildTree().sort(compareBy { it.node?.title?.lowercase() }) }
//            .let { if (state.config.showRoot) it.plus(ROOT_PLAYLIST_DUMMY) else it }

        val channelPlaylists = state.playlists.filter { state.channelPlaylistIds.contains(it.id) }
            .sortedWith(
                compareBy(
                    { it.id != pinnedId },
                    { !it.starred },
                    { it.title.lowercase() })
            ).toMutableList()
//            .apply { if (state.config.showRoot) add(0, ROOT_PLAYLIST_DUMMY) }

        val playlistStats = playlistStatsOrchestrator
            .loadList(IdListFilter(state.playlists.mapNotNull { it.id }), LOCAL.flatOptions())

        val recentLocalPlaylists = recentLocalPlaylists
            .buildRecentSelectionList()
            .let { it.subList(0, min(10, it.size)) }
            .mapNotNull { recentId -> state.playlists.find { it.id == recentId.id } }

        state.playlists.map { it.id }
            .associateWith { id -> playlistStats.find { it.playlistId == id } }
            .let {
                state.playlistsModel =
                    modelMapper.map(
                        channelPlaylists,
                        recentLocalPlaylists,
                        null,
                        pinnedId,
                        state.treeRoot,
                        it,
                        state.config.showRoot
                    )
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
    }

    override fun onAddPlaylist() {
        (state.playlists.size + 1)
            .apply { state.config.itemClick(ADD_PLAYLIST_DUMMY, true) }
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