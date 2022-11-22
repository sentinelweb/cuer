package uk.co.sentinelweb.cuer.app.ui.playlists

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistStatsOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ID
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.SOURCE
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST_CREATE
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.MviStore.*
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.MviStore.Label.Navigate
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviStoreFactory.Action.Init
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain
import uk.co.sentinelweb.cuer.domain.ext.buildLookup
import uk.co.sentinelweb.cuer.domain.ext.buildTree
import uk.co.sentinelweb.cuer.domain.ext.sort

class PlaylistsMviStoreFactory(
    private val storeFactory: StoreFactory,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistStatsOrchestrator: PlaylistStatsOrchestrator,
    private val prefsWrapper: MultiPlatformPreferencesWrapper,
    private val newMedia: NewMediaPlayistInteractor,
    private val recentItems: RecentItemsPlayistInteractor,
    private val localSearch: LocalSearchPlayistInteractor,
    private val remoteSearch: YoutubeSearchPlayistInteractor,
    private val recentLocalPlaylists: RecentLocalPlaylists,
    private val starredItems: StarredItemsPlayistInteractor,
    private val unfinishedItems: UnfinishedItemsPlayistInteractor,
) {
    private sealed class Result {
        data class Load(
            val playlists: List<PlaylistDomain>,
            val playlistStats: List<PlaylistStatDomain>,
            val currentPlayingPlaylistId: OrchestratorContract.Identifier<Long>,
            val appLists: Map<PlaylistDomain, PlaylistStatDomain>,
            val buildRecentSelectionList: List<OrchestratorContract.Identifier<Long>>,
            val pinnedPlaylistId: Long?,
            val treeRoot: PlaylistTreeDomain,
            val treeLookup: Map<Long, PlaylistTreeDomain>
        ) : Result()

        object Empty : Result()// todo remove
//        object NoVideo : Result()
//        data class State(val state: PlayerStateDomain) : Result()
//        data class SetVideo(val item: PlaylistItemDomain, val playlist: PlaylistDomain? = null) : Result()
//
//        data class Playlist(val playlist: PlaylistDomain) : Result()
//        data class SkipTimes(val fwd: String? = null, val back: String? = null) : Result()
//        data class Screen(val screen: PlayerContract.MviStore.Screen) : Result()
//        data class Position(val pos: Long) : Result()
    }

    private sealed class Action {
        object Init : Action()
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(msg: Result): State =
            when (msg) {
//                is State -> copy(playerState = result.state)
                is Result.Load -> copy(
                    playlists = msg.playlists,
                    playlistStats = msg.playlistStats,
                    currentPlayingPlaylistId = msg.currentPlayingPlaylistId,
                    appLists = msg.appLists,
                    recentPlaylists = msg.buildRecentSelectionList,
                    pinnedPlaylistId = msg.pinnedPlaylistId,
                    treeRoot = msg.treeRoot,
                    treeLookup = msg.treeLookup
                )

                else -> copy()
            }
    }

    private class BootstrapperImpl() :
        CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Init)
        }
    }

    private inner class ExecutorImpl
        : CoroutineExecutor<Intent, Action, State, Result, Label>() {

        override fun executeAction(action: Action, getState: () -> State) =
            when (action) {
                Init -> refresh()
            }

        override fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                Intent.Refresh -> refresh()
                Intent.CreatePlaylist ->
                    publish(Navigate(NavigationModel(PLAYLIST_CREATE, mapOf(SOURCE to LOCAL))))

                is Intent.OpenPlaylist -> openPlaylist(intent.item)
                else -> dispatch(Result.Empty)
            }

        private fun openPlaylist(item: PlaylistsItemMviContract.Model) {
            if (item is PlaylistsItemMviContract.Model.ItemModel) {
                recentLocalPlaylists.addRecentId(item.id)
                // fixme move lasttab to shared
                //prefsWrapper.lastBottomTab = MainContract.LastTab.PLAYLIST.ordinal
                publish(
                    Navigate(NavigationModel(PLAYLIST, mapOf(SOURCE to item.source, PLAYLIST_ID to item.id)))
                )
            } else Unit
        }

        private fun refresh() {
            scope.launch {
                executeRefresh()
            }
        }

        private suspend fun executeRefresh() = withContext(coroutines.IO) {
            try {
                val playlists =
                    playlistOrchestrator.loadList(OrchestratorContract.Filter.AllFilter, LOCAL.flatOptions())
                val treeRoot = playlists.buildTree()
                    .sort(compareBy { it.node?.title?.lowercase() })
                val treeLookup = treeRoot.buildLookup()


                val ids = playlists.mapNotNull { if (it.type != PlaylistDomain.PlaylistTypeDomain.APP) it.id else null }
                val playlistStats = playlistStatsOrchestrator
                    .loadList(OrchestratorContract.Filter.IdListFilter(ids), LOCAL.flatOptions())
                    .toMutableList()

                val appLists = mutableListOf(newMedia, recentItems, starredItems, unfinishedItems)
                    .apply { if (prefsWrapper.hasLocalSearch) add(localSearch) }
                    .apply { if (prefsWrapper.hasRemoteSearch) add(remoteSearch) }
                    .map { it.makeHeader() to it.makeStats() }
                    .toMap()

                withContext(scope.coroutineContext) {
                    dispatch(
                        Result.Load(
                            playlists,
//                        .associateWith { pl -> playlistStats.find { it.playlistId == pl.id } },
                            playlistStats,
                            prefsWrapper.currentPlayingPlaylistId,
                            appLists,
                            recentLocalPlaylists.buildRecentSelectionList(),
                            prefsWrapper.pinnedPlaylistId,
                            treeRoot,
                            treeLookup
                        )
                    )
                }
//                modelMapper.map(
//                    state.playlists
//                        .associateWith { pl -> state.playlistStats.find { it.playlistId == pl.id } },
//                    queue.playlistId,
//                    appLists,
//                    recentLocalPlaylists.buildRecentSelectionList(),
//                    prefsWrapper.pinnedPlaylistId,
//                    state.treeRoot
//                ).takeIf { coroutines.mainScopeActive }
//                    ?.also { view.setList(it, false) }
            } catch (e: Exception) {
                log.e("Load failed", e)
                publish(Label.Error("Load failed", e))
//                view.showError("Load failed: ${e::class.java.simpleName}")
//                view.hideRefresh()
            }
        }
    }

    fun create(): PlaylistsMviContract.MviStore =
        object : PlaylistsMviContract.MviStore, Store<Intent, State, Label> by storeFactory.create(
            name = "PlaylistsMviContract",
            initialState = State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl
        ) {}
}
