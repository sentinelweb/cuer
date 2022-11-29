package uk.co.sentinelweb.cuer.app.ui.playlist

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.AllFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.MviStore.*
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.MviStore.Label.Error
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.UndoType.ItemDelete
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
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
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
    private val appPlaylistInteractors: Map<Long, AppPlaylistInteractor>,
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

        data class SetDeletedItem(val item: PlaylistItemDomain?) : Result()
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

                is Result.SetDeletedItem -> copy(deletedPlaylistItem = msg.item)
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
                is Intent.CheckToSave -> checkToSave(intent, getState())
                is Intent.Commit -> commit(intent, getState())
                is Intent.DeleteItem -> delete(intent, getState())
                is Intent.Edit -> edit(intent, getState())
                is Intent.GotoPlaylist -> gotoPlaylist(intent, getState())
                is Intent.Help -> help(intent, getState())
                is Intent.Launch -> launch(intent, getState())
                is Intent.Move -> moveItem(intent, getState())
                is Intent.MoveSwipe -> performMove(intent, getState())
                is Intent.ClearMove -> clearMove(intent, getState())
                is Intent.Pause -> pause(intent, getState())
                is Intent.Play -> play(intent, getState())
                is Intent.PlayItem -> playItem(intent, getState())
                is Intent.PlayModeChange -> playModeChange(intent, getState())
                is Intent.PlaylistSelected -> playlistSelected(intent, getState())
                is Intent.RelatedItem -> relatedItems(intent, getState())
                is Intent.Resume -> resume(intent, getState())
                is Intent.Share -> share(intent, getState())
                is Intent.ShareItem -> shareItem(intent, getState())
                is Intent.ShowCards -> showCards(intent, getState())
                is Intent.ShowChannel -> showChannel(intent, getState())
                is Intent.ShowItem -> showItem(intent, getState())
                is Intent.Star -> star(intent, getState())
                is Intent.StarItem -> starItem(intent, getState())
                is Intent.Undo -> undo(intent, getState())
                is Intent.Update -> update(intent, getState())
                is Intent.UpdatesMedia -> flowUpdatesMedia(intent, getState())
                is Intent.UpdatesPlaylist -> flowUpdatesPlaylist(intent, getState())
                is Intent.UpdatesPlaylistItem -> flowUpdatesPlaylistItem(intent, getState())
            }

        private fun checkToSave(intent: Intent.CheckToSave, state: State): Unit {

        }

        private fun commit(intent: Intent.Commit, state: State): Unit {

        }

        private fun edit(intent: Intent.Edit, state: State): Unit {

        }

        private fun gotoPlaylist(intent: Intent.GotoPlaylist, state: State): Unit {

        }

        private fun help(intent: Intent.Help, state: State): Unit {

        }

        private fun launch(intent: Intent.Launch, state: State): Unit {

        }

        private fun moveItem(intent: Intent.Move, state: State): Unit {

        }

        private fun performMove(intent: Intent.MoveSwipe, state: State): Unit {

        }

        private fun clearMove(intent: Intent.ClearMove, state: State): Unit {

        }

        private fun pause(intent: Intent.Pause, state: State): Unit {

        }

        private fun play(intent: Intent.Play, state: State): Unit {

        }

        private fun playItem(intent: Intent.PlayItem, state: State): Unit {

        }

        private fun playModeChange(intent: Intent.PlayModeChange, state: State): Unit {

        }

        private fun playlistSelected(intent: Intent.PlaylistSelected, state: State): Unit {

        }

        private fun relatedItems(intent: Intent.RelatedItem, state: State): Unit {

        }

        private fun resume(intent: Intent.Resume, state: State): Unit {

        }

        private fun share(intent: Intent.Share, state: State): Unit {

        }

        private fun shareItem(intent: Intent.ShareItem, state: State): Unit {

        }

        private fun showCards(intent: Intent.ShowCards, state: State): Unit {

        }

        private fun showChannel(intent: Intent.ShowChannel, state: State): Unit {

        }

        private fun showItem(intent: Intent.ShowItem, state: State): Unit {

        }

        private fun star(intent: Intent.Star, state: State): Unit {

        }

        private fun starItem(intent: Intent.StarItem, state: State): Unit {

        }

        private fun update(intent: Intent.Update, state: State): Unit {

        }

        private fun flowUpdatesMedia(intent: Intent.UpdatesMedia, state: State): Unit {

        }

        private fun flowUpdatesPlaylist(intent: Intent.UpdatesPlaylist, state: State): Unit {

        }

        private fun flowUpdatesPlaylistItem(intent: Intent.UpdatesPlaylistItem, state: State): Unit {

        }

        private fun State.playlistItemDomain(itemModel: PlaylistItemMviContract.Model.Item) = model
            ?.itemsIdMap
            ?.get(itemModel.id)

        private fun delete(intent: Intent.DeleteItem, state: State): Unit {
            scope.launch {
                delay(400) // waits for ui animation

                state.playlistItemDomain(intent.item)
                    ?.takeIf { it.id != null }
                    ?.also { log.d("found item ${it.id}") }
                    ?.let { deleteItem ->
                        dispatch(Result.SetDeletedItem(deleteItem))
                        val appPlaylistInteractor = appPlaylistInteractors[state.playlist?.id]
                        val action = appPlaylistInteractor?.customResources?.customDelete?.label ?: "Deleted"
                        if (state.playlist?.type != PlaylistDomain.PlaylistTypeDomain.APP
                            || !(appPlaylistInteractor?.hasCustomDeleteAction ?: false)
                        ) {
                            playlistItemOrchestrator.delete(deleteItem, LOCAL.flatOptions())
                        } else {
                            appPlaylistInteractor?.performCustomDeleteAction(deleteItem)
                            executeRefresh(state = state)
                        }
//                        view.showUndo(
//                            "$action: ${deleteItem.media.title}",
//                            ::undoDelete
//                        )
                        publish(Label.ShowUndo(ItemDelete, "$action: ${deleteItem.media.title}"))
                    }
            }
        }

        private fun undo(intent: Intent.Undo, state: State): Unit {
            when (intent.undoType) {
                ItemDelete ->
                    state.deletedPlaylistItem?.let { itemDomain ->
                        scope.launch {
                            playlistItemOrchestrator.save(itemDomain, LOCAL.deepOptions())
                            //state.deletedPlaylistItem = null
                            dispatch(Result.SetDeletedItem(null))
                            executeRefresh(state = state)
                        }
                    }
            }
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

        // region loadRefresh
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
        // endregion loadRefresh
    }

    fun create(): PlaylistMviContract.MviStore =
        object : PlaylistMviContract.MviStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = "PlaylistMviContract.MviStore",
                initialState = State(),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl() },
                reducer = ReducerImpl
            ) {}
}
