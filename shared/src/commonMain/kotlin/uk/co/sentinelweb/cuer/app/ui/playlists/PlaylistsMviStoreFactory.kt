package uk.co.sentinelweb.cuer.app.ui.playlists

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.LocalSearch
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.YoutubeSearch
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.*
import uk.co.sentinelweb.cuer.app.ui.main.MainCommonContract
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.MviStore.*
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.MviStore.Label.Navigate
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.MviStore.Label.ShowUndo
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.UndoType.PlaylistDelete
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.UndoType.SearchDelete
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviStoreFactory.Action.Init
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract
import uk.co.sentinelweb.cuer.app.usecase.PlaylistMergeUsecase
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.app.util.wrapper.PlatformLaunchWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ShareWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.ext.buildLookup
import uk.co.sentinelweb.cuer.domain.ext.buildTree
import uk.co.sentinelweb.cuer.domain.ext.isAncestor
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
    private val liveUpcomingItems: LiveUpcomingItemsPlayistInteractor,
    private val strings: PlaylistsMviContract.Strings,
    private val platformLauncher: PlatformLaunchWrapper,
    private val shareWrapper: ShareWrapper,
    private val merge: PlaylistMergeUsecase,
) {
    private sealed class Result {
        data class Load(
            val playlists: List<PlaylistDomain>,
            val playlistStats: List<PlaylistStatDomain>,
            val currentPlayingPlaylistId: Identifier<GUID>,
            val appLists: Map<PlaylistDomain, PlaylistStatDomain>,
            val buildRecentSelectionList: List<Identifier<GUID>>,
            val pinnedPlaylistId: GUID?,
            val treeRoot: PlaylistTreeDomain,
            val treeLookup: Map<Identifier<GUID>, PlaylistTreeDomain>
        ) : Result()

        data class SetDeletedItem(val item: Domain?) : Result()
        data class SetMoveState(val from: Int?, val to: Int?) : Result()
    }

    private sealed class Action {
        object Init : Action()
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(msg: Result): State =
            when (msg) {
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

                is Result.SetDeletedItem -> copy(deletedItem = msg.item)
                is Result.SetMoveState -> copy(dragFrom = msg.from, dragTo = msg.to)
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
                Intent.CreatePlaylist -> openCreatePlaylist()
                is Intent.OpenPlaylist -> openPlaylist(intent)
                is Intent.Edit -> openEdit(intent)
                is Intent.Delete -> performDelete(intent, getState())
                is Intent.Undo -> performUndo(intent, getState())
                is Intent.MoveSwipe -> performMove(intent, getState())
                is Intent.Move -> moveItem(intent, getState())
                is Intent.ClearMove -> clearMoveState(getState())
                is Intent.Play -> play(intent, getState())
                is Intent.Share -> share(intent, getState())
                is Intent.Star -> star(intent, getState())
                is Intent.Merge -> showMerge(intent, getState())
            }

        // region open
        private fun openCreatePlaylist() { // Intent.CreatePlaylist
            publish(Navigate(NavigationModel(PLAYLIST_CREATE, mapOf(SOURCE to LOCAL)), null))
        }

        private fun openPlaylist(intent: Intent.OpenPlaylist) {
            if (intent.item is PlaylistsItemMviContract.Model.Item) {
                recentLocalPlaylists.addRecentId(intent.item.id.id)
                prefsWrapper.lastBottomTab = MainCommonContract.LastTab.PLAYLIST.ordinal
                publish(
                    Navigate(
                        NavigationModel(
                            PLAYLIST, mapOf(
                                SOURCE to intent.item.source,
                                PLAYLIST_ID to intent.item.id.id.value,
                                IMAGE_URL to intent.item.thumbNailUrl
                            )
                        ),
                        intent.view
                    )
                )
            } else Unit
        }

        private fun openEdit(intent: Intent.Edit) {
            if (intent.item is PlaylistsItemMviContract.Model.Item) {
                recentLocalPlaylists.addRecentId(intent.item.id.id)
                publish(
                    Navigate(
                        NavigationModel(PLAYLIST_EDIT, mapOf(SOURCE to intent.item.source, PLAYLIST_ID to intent.item.id.id.value)),
                        intent.view
                    )
                )
            } else Unit
        }
        // endregion

        // region play
        private fun play(intent: Intent.Play, state: State) {
            if (!intent.external) {
                if (intent.item is PlaylistsItemMviContract.Model.Item) {
                    recentLocalPlaylists.addRecentId(intent.item.id.id)
                    prefsWrapper.lastBottomTab = MainCommonContract.LastTab.PLAYLIST.ordinal
                    publish(
                        Navigate(
                            NavigationModel(PLAYLIST, mapOf(SOURCE to intent.item.source, PLAYLIST_ID to intent.item.id.id.value, PLAY_NOW to true)),
                            intent.view
                        )
                    )
                } else Unit
            } else {
                state.findPlaylist(intent.item)
                    ?.takeIf { it.type == PlaylistDomain.PlaylistTypeDomain.PLATFORM }
                    ?.apply { platformLauncher.launchPlaylist(platformId!!) }
                    ?: let { publish(Label.Error(strings.playlists_error_cant_play)) }
            }
        }
        // endregion

        // region actions
        private fun star(intent: Intent.Star, state: State) {
            scope.launch {
                state.findPlaylist(intent.item)
                    ?.takeIf { it.id?.let { it.source == LOCAL } ?: false }
                    ?.let { it.copy(starred = !it.starred) }
                    ?.also { playlistOrchestrator.save(it, LOCAL.flatOptions()) }
            }
        }

        private fun share(intent: Intent.Share, state: State) {
            state.findPlaylist(intent.item)
                ?.takeIf { it.id?.let { it.source == LOCAL } ?: false && it.type != PlaylistDomain.PlaylistTypeDomain.APP }
                ?.let { itemDomain ->
                    scope.launch {
                        playlistOrchestrator.loadById(itemDomain.id!!.id, LOCAL.deepOptions())
                            ?.also { shareWrapper.share(it) }
                            ?: publish(Label.Error(strings.playlists_error_cant_load))
                    }
                }
        }

        private fun showMerge(intent: Intent.Merge, state: State) {
            state.findPlaylist(intent.item)
                ?.takeIf { it.type != PlaylistDomain.PlaylistTypeDomain.APP }
                ?.also { delPlaylist ->
                    PlaylistsMviDialogContract.Config(
                        title = strings.playlist_dialog_title,
                        selectedPlaylists = setOf(),
                        multi = true,
                        itemClick = { p, _ -> merge(p!!, delPlaylist) },
                        confirm = { },
                        dismiss = { },
                        suggestionsMedia = null,
                        showPin = false,
                        showAdd = false
                    ).also { publish(Label.ShowPlaylistsSelector(it)) }
                }
        }

        private fun merge(thisPlaylist: PlaylistDomain, delPlaylist: PlaylistDomain) {
            scope.launch {
                if (merge.checkMerge(thisPlaylist, delPlaylist)) {
                    merge.merge(thisPlaylist, delPlaylist)
                } else {
                    publish(Label.Error(strings.playlists_error_cant_merge))
                }
            }
        }
        // endregion

        // region delete/undo
        private fun performDelete(intent: Intent.Delete, state: State) {
            scope.launch {
                delay(400)
                state.findPlaylist(intent.item)
                    ?.also { playlist ->
                        if (playlist.type != PlaylistDomain.PlaylistTypeDomain.APP) {
                            val treeDomain = state.treeLookup[playlist.id]!!
                            if (treeDomain.node == null) {
                                //publish(Label.Error(strings.playlists_error_delete_children))
                                log.e(
                                    "Attempted to delete root node shouldn't be able to do this",
                                    NullPointerException()
                                )
                            } else if (treeDomain.chidren.size > 0) {
                                publish(Label.Error(strings.playlists_error_delete_children))
                            } else if (treeDomain.node?.default ?: false) {
                                publish(Label.Error(strings.playlists_error_delete_default))
                            } else {
                                playlist.id
                                    ?.let { playlistOrchestrator.loadById(it.id, LOCAL.deepOptions()) }
                                    ?.apply {
                                        playlistOrchestrator.delete(playlist, LOCAL.flatOptions())
                                        publish(Label.ItemRemoved(intent.item))
                                        publish(
                                            ShowUndo(PlaylistDelete, strings.playlists_message_deleted(playlist.title))
                                        )
                                    }
                                    ?.also { dispatch(Result.SetDeletedItem(it)) }
                                    ?: let {
                                        publish(Label.Error(strings.playlists_error_cant_backup))
                                        null
                                    }

                            }
                        } else if (playlist.id?.id == LocalSearch.id || playlist.id?.id == YoutubeSearch.id) {
                            val isLocal = playlist.id?.id == LocalSearch.id
                            val type = if (isLocal) strings.search_local else strings.search_youtube
                            if (isLocal) {
                                dispatch(Result.SetDeletedItem(prefsWrapper.lastLocalSearch))
                            } else {
                                dispatch(Result.SetDeletedItem(prefsWrapper.lastRemoteSearch))
                            }
                            if (isLocal) prefsWrapper.lastLocalSearch = null
                            else prefsWrapper.lastRemoteSearch = null
                            if (!isLocal) remoteSearch.clearCached()
                            publish(ShowUndo(SearchDelete, strings.playlists_message_deleted_search(type)))
                            executeRefresh()
                        }
                    } ?: let { publish(Label.Error(strings.playlists_error_cant_delete)) }
            }
        }

        private fun performUndo(intent: Intent.Undo, state: State) = when (intent.undoType) {
            PlaylistDelete -> (state.deletedItem as? PlaylistDomain)
                ?.let { itemDomain ->
                    scope.launch {
                        playlistOrchestrator.save(itemDomain, LOCAL.deepOptions())
                        dispatch(Result.SetDeletedItem(null))
                        refresh()
                    }
                    Unit
                } ?: Unit

            SearchDelete -> state.deletedItem?.let {
                when (it) {
                    is SearchLocalDomain -> prefsWrapper.lastLocalSearch = it
                    is SearchRemoteDomain -> prefsWrapper.lastRemoteSearch = it
                }
                dispatch(Result.SetDeletedItem(null))
                refresh()
            } ?: Unit
        }
        // endregion

        // region move
        private fun moveItem(intent: Intent.Move, state: State) {
            dispatch(Result.SetMoveState(state.dragFrom ?: intent.fromPosition, intent.toPosition))
        }

        private fun clearMoveState(state: State) {
            if (state.dragFrom == null || state.dragTo == null) {
                refresh()
            }
            dispatch(Result.SetMoveState(null, null))
        }

        private fun performMove(intent: Intent.MoveSwipe, state: State) {
            state.findPlaylist(intent.item)
                ?.takeIf { it.type != PlaylistDomain.PlaylistTypeDomain.APP }
                ?.also { movePlaylist ->
                    PlaylistsMviDialogContract.Config(
                        title = strings.playlist_dialog_title,
                        selectedPlaylists = setOf(),
                        multi = true,
                        itemClick = { playlistSelected, _ ->
                            playlistSelected
                                ?.apply { setParent(this, movePlaylist, state) }
                                ?: openCreatePlaylist()
                        },
                        confirm = { },
                        dismiss = { publish(Label.Repaint) },
                        suggestionsMedia = null,
                        showPin = false,
                        showRoot = true,
                        showAdd = false
                    ).also { publish(Label.ShowPlaylistsSelector(it)) }
                }
        }

        private fun setParent(parent: PlaylistDomain, child: PlaylistDomain, state: State) {
            val childNode = state.treeLookup[child.id]!!
            val parentNode = state.treeLookup[parent.id]
            if (parent.id == null || !childNode.isAncestor(parentNode!!)) {
                scope.launch {
                    playlistOrchestrator.save(child.copy(parentId = parent.id), LOCAL.flatOptions())
                }
            } else {
                publish(Label.Repaint)
                publish(Label.Message(strings.playlists_error_circular))
            }
        }
        // endregion

        // region refresh
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

                val ids = playlists.mapNotNull { if (it.type != PlaylistDomain.PlaylistTypeDomain.APP) it.id?.id else null }
                val playlistStats = playlistStatsOrchestrator
                    .loadList(OrchestratorContract.Filter.IdListFilter(ids), LOCAL.flatOptions())
                    .toMutableList()

                val appLists = mutableListOf(newMedia, recentItems, starredItems, unfinishedItems, liveUpcomingItems)
                    .apply { if (prefsWrapper.hasLocalSearch) add(localSearch) }
                    .apply { if (prefsWrapper.hasRemoteSearch) add(remoteSearch) }
                    .map { it.makeHeader() to it.makeStats() }
                    .toMap()

                log.d("Playlists executeRefresh: playlists:${playlists.size}")

                withContext(scope.coroutineContext) {
                    dispatch(
                        Result.Load(
                            playlists,
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
            } catch (e: Exception) {
                log.e("Load failed", e)
                publish(Label.Error(strings.playlists_error_load_failed, e))
            }
        }

        // endregion refresh


        private fun State.findPlaylist(item: PlaylistsItemMviContract.Model) = playlists.find { it.id == item.id }
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
