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
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.*
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
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain
import uk.co.sentinelweb.cuer.domain.ext.buildLookup
import uk.co.sentinelweb.cuer.domain.ext.buildTree
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator

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
    private val playlistMutator: PlaylistMutator,
    private val util: PlaylistMviUtil,
    private val modelMapper: PlaylistMviModelMapper,
    private val itemModelMapper: PlaylistMviItemModelMapper,
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
            val itemsIdMap: MutableMap<Long, PlaylistItemDomain>,
        ) : Result()

        data class SetDeletedItem(val item: PlaylistItemDomain?) : Result()
        data class SetPlaylist(
            var playlistIdentifier: OrchestratorContract.Identifier<*>,
            val playlist: PlaylistDomain?
        ) : Result()
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
                    itemsIdMap = msg.itemsIdMap,
                )

                is Result.SetDeletedItem -> copy(deletedPlaylistItem = msg.item)
                is Result.SetPlaylist -> copy(playlist = msg.playlist)
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
            log.d("media changed: ${intent.op}, ${intent.source}, id=${intent.media.id} title=${intent.media.title}")
            when (intent.op) {
                FLAT,
                FULL,
                -> {
                    val containsMedia =
                        state.playlist?.items?.find { it.media.platformId == intent.media.platformId } != null
                    if (containsMedia) {
                        updatePlaylistItemMedia(null, intent.media, state)
                    }
                }

                DELETE -> Unit
            }
        }

        private fun flowUpdatesPlaylist(intent: Intent.UpdatesPlaylist, state: State): Unit {

        }

        private fun flowUpdatesPlaylistItem(intent: Intent.UpdatesPlaylistItem, state: State): Unit {
            log.d("item changed: ${intent.op}, ${intent.source}, id=${intent.item.id} media=${intent.item.media.title}")
            val currentIndexBefore = state.playlist?.currentIndex
            when (intent.op) { // todo just apply model updates (instead of full rebuild)
                FLAT,
                FULL,
                -> if (intent.item.playlistId?.toIdentifier(intent.source) == state.playlistIdentifier) {
                    state.playlist
                        ?.let { playlistMutator.addOrReplaceItem(it, intent.item) }
                        ?.takeIf { it != state.playlist }
                        ?.also { state.playlist = it }
                        ?.also { updateView(state) }
                } else if (state.playlist?.type == PlaylistDomain.PlaylistTypeDomain.APP) {// check to replace item in an app playlist
                    state.playlist
                        ?.items
                        ?.find { it.media.platformId == intent.item.media.platformId }
                        ?.also { updatePlaylistItemMedia(intent.item, intent.item.media, state) }
                        ?.let { state.playlist }
                } else {
                    state.playlist
                        ?.let { playlistMutator.remove(it, intent.item) }
                        ?.takeIf { it != state.playlist }
                        ?.also { state.playlist = it }
                        ?.also { updateView(state) }
                }

                DELETE ->
                    state.playlist
                        ?.let { playlistMutator.remove(it, intent.item) }
                        ?.takeIf { it != state.playlist }
                        ?.also { state.playlist = it }
                        ?.also { updateView(state) }
            }.takeIf { !util.isQueuedPlaylist(state) && currentIndexBefore != state.playlist?.currentIndex }
                ?.apply {
                    scope.launch {
                        state.playlist?.apply {
                            playlistOrDefaultUsecase.updateCurrentIndex(this, state.playlistIdentifier.flatOptions())
                        }
                    }
                }
        }

        private fun updateView(state: State, animate: Boolean = true) {
            state.playlist
                .takeIf { coroutines.mainScopeActive }
                ?.let {
                    modelMapper.map(
                        domain = it,
                        isPlaying = util.isPlaylistPlaying(state),
                        id = state.playlistIdentifier,
                        playlists = state.playlistsTreeLookup,
                        pinned = util.isPlaylistPinned(state),
                        appPlaylist = state.playlist?.id?.let { appPlaylistInteractors[it] },
                        itemsIdMap = state.itemsIdMap
                    )
                }
                // fix me uncomment - dont map model need to move out id generation to here and keep just the idmap. i think..
//                ?.also { state.model = it }
//                ?.also { view.setModel(it, animate) }
                .also {
                    state.focusIndex?.apply {
                        //view.scrollToItem(this)
                        publish(Label.ScrollToItem(this))
                        state.focusIndex = null
                    }
                }.also {
                    state.playlist
                        ?.currentIndex
                        ?.also {
                            publish(Label.HighlightPlayingItem(it))
                            //view.highlightPlayingItem(it)
                        }
                }
        }

        private fun updatePlaylistItemMedia(plistItem: PlaylistItemDomain?, media: MediaDomain, state: State) {
            state.playlist
                ?.items
                ?.apply {
                    indexOfFirst { it.media.platformId == media.platformId }
                        .takeIf { it > -1 }
                        ?.let { index ->
                            state.model?.let { model ->
                                val originalItemDomain = get(index)
                                val changedItemDomain =
                                    plistItem ?: originalItemDomain.copy(media = media)
                                //model.itemsIdMap.keys.associateBy { model.itemsIdMap[it] }[originalItem]
                                state.itemsIdMap.entries.firstOrNull {
                                    if (originalItemDomain.id != null) {
                                        it.value.id == originalItemDomain.id
                                    } else {
                                        it.value == originalItemDomain
                                    }
                                }?.key
                                    ?.also { updateItem(index, it, changedItemDomain, state) }
                                    ?: throw Exception("Couldn't lookup model ID for $originalItemDomain keys=${state.itemsIdMap.keys}")
                            }
                        }
                }
        }

        private fun updateItem(
            index: Int,
            modelId: Long,
            changedItem: PlaylistItemDomain,
            state: State,
        ) {
            val newPlaylist = state.playlist?.let {
                it.copy(items = it.items.toMutableList().apply { set(index, changedItem) })
            }

            val mappedItem = itemModelMapper.mapItem(
                modelId, changedItem, index,
                newPlaylist?.config?.editableItems ?: false,
                newPlaylist?.config?.deletableItems ?: false,
                newPlaylist?.config?.editable ?: false,
                playlistText = modelMapper.mapPlaylistText(
                    changedItem,
                    newPlaylist,
                    state.playlistsTreeLookup
                ),
                showOverflow = true,
                deleteResources = newPlaylist?.id?.let { appPlaylistInteractors[it] }?.customResources?.customDelete
            )
            state.model = state.model
                ?.let {
                    it.copy(items = it.items
                        ?.toMutableList()
                        ?.apply { set(index, mappedItem) })
                }
                ?.also { state.itemsIdMap[modelId] = changedItem }

            mappedItem
                .takeIf { coroutines.mainScopeActive }
            // fixme label
            //?.apply { view.updateItemModel(this) }
        }

        private fun State.playlistItemDomain(itemModel: PlaylistItemMviContract.Model.Item) =
            itemsIdMap.get(itemModel.id)
        // fixme figure out if how cache models?
        //playlist?.items?.find { it.id == itemModel.id }

        private fun delete(intent: Intent.DeleteItem, state: State): Unit {
            scope.launch {
                delay(400) // waits for ui animation
                log.d("delete item: ${intent.item.id} ${intent.item.title}")
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
                            log.d("deleting item: ${intent.item.id}")
                            playlistItemOrchestrator.delete(deleteItem, LOCAL.flatOptions())
                            log.d("deleted item: ${intent.item.id}")
                        } else {
                            appPlaylistInteractor?.performCustomDeleteAction(deleteItem)
                            executeRefresh(state = state)
                        }
                        log.d("deleted item: ${intent.item.id}")
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
                        } else state.focusIndex,
                        itemsIdMap = buildIdList(playlistOrDefault.first)
                    )
                )
            } catch (e: Throwable) {
                log.e("Error loading playlist", e)
                publish(Error("Load failed: ${e::class.simpleName}"))
                publish(Label.Loaded)
            }
        }

        fun buildIdList(domain: PlaylistDomain?): MutableMap<Long, PlaylistItemDomain> {
            val modelIdGenerator = IdGenerator()
            val itemsIdMap = mutableMapOf<Long, PlaylistItemDomain>()
            domain?.items?.mapIndexed { index, item ->
                val modelId = item.id ?: modelIdGenerator.value
                itemsIdMap[modelId] = item
            }
            return itemsIdMap
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
