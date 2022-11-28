package uk.co.sentinelweb.cuer.app.ui.playlist

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.AllFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.MviStore.*
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.MviStore.Label.Error
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviStoreFactory.Action.Init
import uk.co.sentinelweb.cuer.app.usecase.PlaylistOrDefaultUsecase
import uk.co.sentinelweb.cuer.app.usecase.PlaylistUpdateUsecase
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.app.util.wrapper.PlatformLaunchWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ShareWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain
import uk.co.sentinelweb.cuer.domain.ext.buildLookup
import uk.co.sentinelweb.cuer.domain.ext.buildTree

class PlaylistMviStoreFactory(
    private val storeFactory: StoreFactory,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistStatsOrchestrator: PlaylistStatsOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val mediaOrchestrator: MediaOrchestrator,
    private val playlistUpdateUsecase: PlaylistUpdateUsecase,
    private val playlistOrDefaultUsecase: PlaylistOrDefaultUsecase,
    private val prefsWrapper: MultiPlatformPreferencesWrapper,
    private val platformLauncher: PlatformLaunchWrapper,
    private val shareWrapper: ShareWrapper,
    private val dbInit: DatabaseInitializer,
    private val recentLocalPlaylists: RecentLocalPlaylists,
    private val queue: QueueMediatorContract.Producer,
) {
    init {
        log.tag(this)
        log.d("init PlaylistMviStoreFactory")
    }

    private sealed class Result {
        data class Load(
            var playlistIdentifier: OrchestratorContract.Identifier<*>,
            var playlist: PlaylistDomain?,
            var focusIndex: Int?,
            var playlistsTree: PlaylistTreeDomain?,
            var playlistsTreeLookup: Map<Long, PlaylistTreeDomain>?,
        ) : Result()

//        data class SetDeletedItem(val item: Domain?) : Result()
//        data class SetMoveState(val from: Int?, val to: Int?) : Result()
    }

    private sealed class Action {
        object Init : Action()
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(msg: Result): State =
            when (msg) {
                is Result.Load -> copy(
                    playlistIdentifier = msg.playlistIdentifier,
                    playlist = msg.playlist,
                    focusIndex = msg.focusIndex,
                    playlistsTree = msg.playlistsTree,
                    playlistsTreeLookup = msg.playlistsTreeLookup,
                )

                //is Result.SetDeletedItem -> copy(deletedItem = msg.item)
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
                Init -> refresh(getState())
            }

        override fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                is Intent.Refresh -> refresh(getState())
                is Intent.SetPlaylistData -> setPlaylistData(intent, getState())
//                Intent.CreatePlaylist -> openCreatePlaylist()
//                is Intent.OpenPlaylist -> openPlaylist(intent)
//                is Intent.Edit -> openEdit(intent)
//                is Intent.Delete -> performDelete(intent, getState())
//                is Intent.Undo -> performUndo(intent, getState())
//                is Intent.MoveSwipe -> performMove(intent, getState())
//                is Intent.Move -> moveItem(intent, getState())
//                is Intent.ClearMove -> clearMoveState(getState())
//                is Intent.Play -> play(intent, getState())
//                is Intent.Share -> share(intent, getState())
//                is Intent.Star -> star(intent, getState())
//                is Intent.Merge -> showMerge(intent, getState())
            }

        // region open
//        private fun openCreatePlaylist() { // Intent.CreatePlaylist
//            publish(Navigate(NavigationModel(PLAYLIST_CREATE, mapOf(SOURCE to LOCAL)), null))
//        }
//
//        private fun openPlaylist(intent: Intent.OpenPlaylist) {
//            if (intent.item is PlaylistsItemMviContract.Model.Item) {
//                recentLocalPlaylists.addRecentId(intent.item.id)
//                prefsWrapper.lastBottomTab = MainCommonContract.LastTab.PLAYLIST.ordinal
//                publish(
//                    Navigate(
//                        NavigationModel(PLAYLIST, mapOf(SOURCE to intent.item.source, PLAYLIST_ID to intent.item.id)),
//                        intent.view
//                    )
//                )
//            } else Unit
//        }

        // endregion

        private fun setPlaylistData(intent: Intent.SetPlaylistData, state: State) {
            // fixme bit dodgy here - modifying state.
            coroutines.mainScope.launch {
                val notLoaded = state.playlist == null
                intent.plId
                    ?.takeIf { it != -1L }
                    ?.toIdentifier(intent.source)
                    ?.apply {
                        state.playlistIdentifier = this
                    }
                    ?.apply { executeRefresh(scrollToCurrent = notLoaded, state = state) }
                    ?.apply {
                        if (intent.playNow) {
                            queue.playNow(state.playlistIdentifier, intent.plItemId)
                        }
                    }
                    ?.apply {
                        state.playlist?.also { recentLocalPlaylists.addRecent(it) }
                    }
                    ?: run {
                        if (dbInit.isInitialized()) {
                            state.playlistIdentifier = prefsWrapper.lastViewedPlaylistId
                            executeRefresh(scrollToCurrent = notLoaded, state = state)
                        } else {
                            dbInit.addListener { b: Boolean ->
                                if (b) {
                                    state.playlistIdentifier = 3L.toIdentifier(LOCAL) // philosophy
                                    //updatePlaylist()
                                    refresh(state = state)
                                }
                            }
                        }
                    }
            }
        }

        private fun refresh(state: State) {
            scope.launch { executeRefresh(false, false, state) }
        }

        private suspend fun executeRefresh(animate: Boolean = true, scrollToCurrent: Boolean = false, state: State) {
            //view.showRefresh()
            publish(Label.Loading)
            try {
                val id = state.playlistIdentifier
                    .takeIf { it != OrchestratorContract.NO_PLAYLIST }
                    ?: prefsWrapper.currentPlayingPlaylistId
                val playlistOrDefault = playlistOrDefaultUsecase
                    .getPlaylistOrDefault(id)
                val playlistsTree = playlistOrchestrator
                    .loadList(AllFilter, LOCAL.flatOptions())
                    .buildTree()
                dispatch(
                    Result.Load(
                        playlist = playlistOrDefault?.first,
                        playlistIdentifier = playlistOrDefault?.first
                            ?.id?.toIdentifier(playlistOrDefault.second)
                            ?: throw IllegalStateException("Need an id"),
                        playlistsTree = playlistsTree,
                        playlistsTreeLookup = playlistsTree.buildLookup(),
                        focusIndex = if (scrollToCurrent && state.focusIndex == null) {
                            state.playlist?.currentIndex
                        } else state.focusIndex
                    )
                )
            } catch (e: Throwable) {
                log.e("Error loading playlist", e)
                publish(Error("Load failed: ${e::class.simpleName}"))
                publish(Label.Loaded)
            }
        }

    }

    fun create(): PlaylistMviContract.MviStore =
        object : PlaylistMviContract.MviStore,
            Store<Intent, State, PlaylistMviContract.MviStore.Label> by storeFactory.create(
                name = "PlaylistMviContract.MviStore",
                initialState = State(),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl() },
                reducer = ReducerImpl
            ) {}
}
