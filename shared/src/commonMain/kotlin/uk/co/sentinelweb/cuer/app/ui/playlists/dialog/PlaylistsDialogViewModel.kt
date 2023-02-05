package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistStatsOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract.Companion.ADD_PLAYLIST_DUMMY
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract.Companion.ROOT_PLAYLIST_DUMMY
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract.Label.Dismiss
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ext.buildTree
import uk.co.sentinelweb.cuer.domain.ext.sort
import kotlin.math.min

class PlaylistsDialogViewModel(
    private val state: PlaylistsMviDialogContract.State,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistStatsOrchestrator: PlaylistStatsOrchestrator,
    private val modelMapper: PlaylistsModelMapper,
    private val dialogModelMapper: PlaylistsDialogModelMapper,
    private val log: LogWrapper,
    private val prefsWrapper: MultiPlatformPreferencesWrapper,
    private val coroutines: CoroutineContextProvider,
    private val recentLocalPlaylists: RecentLocalPlaylists,
) {
    init {
        log.tag(this)
    }

    private val _model: MutableStateFlow<PlaylistsMviDialogContract.Model>
    val model: Flow<PlaylistsMviDialogContract.Model>
        get() = _model
//            .onSubscription { log.d("model.onSubscription") }
//            .onStart { log.d("model.onStart") }
//            .onEach { log.d("model.onEach(items:${it.playistsModel?.items?.size})") }
//            .onCompletion { log.d("model.onCompletion") }

    private val _label: MutableSharedFlow<PlaylistsMviDialogContract.Label>
    val label: Flow<PlaylistsMviDialogContract.Label> get() = _label

    init {
        _model = MutableStateFlow(PlaylistsMviDialogContract.Model(null, false, false, false))
        _label = MutableSharedFlow()
    }

    fun refreshList() {
        refreshPlaylists()
    }

    fun onItemClicked(item: PlaylistsItemMviContract.Model) {
        val findId = item.id.takeIf { it.source == LOCAL }?.id// null finds top level node
        findId
            ?.let { state.playlists.find { it.id?.id == findId } }
            ?.apply { if (state.pinWhenSelected) prefsWrapper.pinnedPlaylistId = id!!.id }
            ?.apply { state.config.itemClick(this, true) }
            ?.also { dismiss() }
            ?: apply { state.config.itemClick(ROOT_PLAYLIST_DUMMY, true) }
                .also { dismiss() }
    }

    private fun dismiss() {
        coroutines.mainScope.launch {
            _label.emit(Dismiss)
        }
    }

    private fun refreshPlaylists() {
        coroutines.mainScope.launch { executeRefresh() }
    }

    private suspend fun executeRefresh() {
        state.channelPlaylistIds.clear()
        state.config.suggestionsMedia?.apply {
            playlistOrchestrator.loadList(ChannelPlatformIdFilter(this.channelData.platformId!!), LOCAL.flatOptions())
                .apply { state.channelPlaylistIds.addAll(this.map { it.id!! }) }
        }

        val pinnedId = prefsWrapper.pinnedPlaylistId
        state.playlists = playlistOrchestrator.loadList(AllFilter, LOCAL.flatOptions())
            .filter { it.config.editableItems }
            .apply { state.treeRoot = buildTree().sort(compareBy { it.node?.title?.lowercase() }) }

        val channelPlaylists = state.playlists.filter { state.channelPlaylistIds.contains(it.id) }
            .sortedWith(
                compareBy(
                    { it.id?.id != pinnedId },
                    { !it.starred },
                    { it.title.lowercase() })
            ).toMutableList()

        val playlistStats = playlistStatsOrchestrator
            .loadList(IdListFilter(state.playlists.mapNotNull { it.id?.id }), LOCAL.flatOptions())

        val recentLocalPlaylists = recentLocalPlaylists
            .buildRecentSelectionList()
            .let { it.subList(0, min(10, it.size)) }
            .mapNotNull { recentId -> state.playlists.find { it.id?.id == recentId.id } }

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
            ?.also {
                log.d("emit model")
                _model.emit(it)
            }
    }

    private fun updateDialogModel() {
        dialogModelMapper.map(state.playlistsModel, state.config, state.pinWhenSelected)
            .takeIf { coroutines.mainScopeActive }
            ?.also {
                coroutines.mainScope.launch { _model.emit(it) }
            }
    }

    fun onPinSelectedPlaylist(b: Boolean) {
        state.pinWhenSelected = b
        updateDialogModel()
    }

    fun onDismiss() {
        state.config.dismiss()
        dismiss()
    }

    fun setConfig(config: PlaylistsMviDialogContract.Config) {
        state.config = config
    }

    fun onAddPlaylist() {
        (state.playlists.size + 1)
            .apply { state.config.itemClick(ADD_PLAYLIST_DUMMY, true) }
            .also { dismiss() }
    }

    fun onResume() {
        log.d("onResume")
        coroutines.mainScope.launch { executeRefresh() }
        playlistOrchestrator.updates
            .onEach { refreshPlaylists() }
            .let { coroutines.mainScope }
    }

    fun onPause() {
        coroutines.cancel()
    }
}