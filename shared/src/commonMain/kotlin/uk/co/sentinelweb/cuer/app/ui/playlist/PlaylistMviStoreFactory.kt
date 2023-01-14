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
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.YoutubeSearch
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ID
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.SOURCE
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringResource
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringResource.playlist_error_updating
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringResource.playlist_items_updated
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.MviStore.*
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.MviStore.Label.Error
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.MviStore.Label.Message
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.UndoType.ItemDelete
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.UndoType.ItemMove
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviStoreFactory.Action.Init
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviStoreFactory.Result.*
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract
import uk.co.sentinelweb.cuer.app.usecase.AddPlaylistUsecase
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
import uk.co.sentinelweb.cuer.app.usecase.PlaylistOrDefaultUsecase
import uk.co.sentinelweb.cuer.app.usecase.PlaylistUpdateUsecase
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.ext.*
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
    private val dbInit: DatabaseInitializer,
    private val recentLocalPlaylists: RecentLocalPlaylists,
    private val queue: QueueMediatorContract.Producer,
    private val appPlaylistInteractors: Map<Long, AppPlaylistInteractor>,
    private val playlistMutator: PlaylistMutator,
    private val util: PlaylistMviUtil,
    private val modelMapper: PlaylistMviModelMapper,
    private val itemModelMapper: PlaylistMviItemModelMapper,
    private val playUseCase: PlayUseCase,
    private val strings: StringDecoder,
    private val timeProvider: TimeProvider,
    private val addPlaylistUsecase: AddPlaylistUsecase,
    private val multiPrefs: MultiPlatformPreferencesWrapper,
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
            val playlist: PlaylistDomain?,
            var playlistIdentifier: OrchestratorContract.Identifier<*>? = null
        ) : Result()

        data class SetPlaylistId(
            var playlistIdentifier: OrchestratorContract.Identifier<*>
        ) : Result()

        data class IdUpdate(val modelId: Long, val changedItem: PlaylistItemDomain) : Result()
        data class SelectedPlaylistItem(val item: PlaylistItemDomain?) : Result()
        data class MovedPlaylistItem(val item: PlaylistItemDomain?) : Result()
        data class SetModified(val modified: Boolean = true) : Result()
        data class SetMoveState(val from: Int?, val to: Int?) : Result()
        data class SetCards(val isCards: Boolean) : Result()
        data class SetFocusIndex(val index: Int) : Result()
        data class SetHeadless(val headless: Boolean) : Result()
    }

    private sealed class Action {
        object Init : Action()
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(msg: Result): State =
            when (msg) {
                is Load -> copy(
                    playlistIdentifier = msg.playlistIdentifier,
                    playlist = msg.playlist,
                    focusIndex = msg.focusIndex,
                    playlistsTree = msg.playlistsTree,
                    playlistsTreeLookup = msg.playlistsTreeLookup,
                    itemsIdMap = msg.itemsIdMap,
                )

                is SetDeletedItem -> copy(deletedPlaylistItem = msg.item)
                is SetPlaylist -> copy(
                    playlist = msg.playlist,
                    playlistIdentifier = msg.playlistIdentifier ?: playlistIdentifier
                )

                is IdUpdate -> copy(itemsIdMap = itemsIdMap.apply { put(msg.modelId, msg.changedItem) })
                is SelectedPlaylistItem -> copy(selectedPlaylistItem = msg.item)
                is MovedPlaylistItem -> copy(movedPlaylistItem = msg.item)
                is SetModified -> copy(isModified = msg.modified)
                is SetMoveState -> copy(dragFrom = msg.from, dragTo = msg.to)
                is SetCards -> copy(isCards = msg.isCards)
                is SetPlaylistId -> copy(playlistIdentifier = msg.playlistIdentifier)
                is SetFocusIndex -> copy(focusIndex = msg.index)
                is SetHeadless -> copy(isHeadless = msg.headless)
                //else -> copy()
            }
    }

    private class BootstrapperImpl() :
        CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Init)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private inner class ExecutorImpl
        : CoroutineExecutor<Intent, Action, State, Result, Label>() {

        override fun executeAction(action: Action, getState: () -> State) =
            when (action) {
                Init -> Unit // refresh(getState())
            }

        override fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                is Intent.Refresh -> refresh(getState())
                is Intent.SetPlaylistData -> setPlaylistData(intent, getState)
                is Intent.CheckToSave -> checkToSave(intent, getState())
                is Intent.CheckToSaveConfirm -> checkToSaveConfirm(intent, getState)
                is Intent.Commit -> commit(intent, getState)
                is Intent.DeleteItem -> delete(intent, getState())
                is Intent.Edit -> edit(intent, getState())
                is Intent.GotoPlaylist -> gotoPlaylist(intent, getState())
                is Intent.Help -> help(intent, getState())
                is Intent.Launch -> launch(intent, getState())
                is Intent.Move -> moveItem(intent, getState())
                is Intent.MoveSwipe -> performMove(intent, getState)
                is Intent.CommitMove -> commitMove(intent, getState())
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
                is Intent.QueueItemFlow -> flowQueueItem(intent, getState())
                is Intent.QueuePlaylistFlow -> flowQueuePlaylist(intent, getState())
                Intent.Headless -> headless(intent, getState())
            }

        private fun headless(intent: Intent, state: State) {
            dispatch(SetHeadless(true))
        }

        private fun flowQueuePlaylist(intent: Intent.QueuePlaylistFlow, state: State) {
//            .filter { isQueuedPlaylist }
//                .onEach { log.d("q.playlist change id=${it.id} index=${it.currentIndex}") }
//                .onEach { view.highlightPlayingItem(it.currentIndex) }
//                .launchIn(coroutines.mainScope)
            if (state.playlistIdentifier == queue.playlistId) {
                intent.item?.currentIndex
                    ?.also { publish(Label.HighlightPlayingItem(it)) }
            }
        }

        private fun flowQueueItem(intent: Intent.QueueItemFlow, state: State) {
            state.playlist?.items
                ?.indexOfFirst { intent.item?.id == it.id }
                ?.also { publish(Label.HighlightPlayingItem(it)) }
//                ?.also { view.highlightPlayingItem(it) }
        }

        private fun checkToSave(intent: Intent.CheckToSave, state: State) {
            if ((state.playlist?.id ?: 0) <= 0 || state.isModified) {
                publish(Label.CheckSaveShowDialog(modelMapper.mapSaveConfirmAlert()))
            } else publish(Label.Navigate(NavigationModel.DONE))
        }

        private fun checkToSaveConfirm(intent: Intent.CheckToSaveConfirm, getState: () -> State) {
            commit(null, getState)
            publish(Label.Navigate(NavigationModel.DONE))
        }

        private fun commit(intent: Intent.Commit?, getState: () -> State) {
            val state = getState()
            if (state.playlistIdentifier.source == MEMORY) {
                log.i("commitPlaylist: id:${state.playlistIdentifier}")
                scope.launch {
                    addPlaylistUsecase
                        .addPlaylist(state.playlist!!, state.addPlaylistParent)
//                    .also {
//                        state.playlistIdentifier =
//                            it.id?.toIdentifier(LOCAL) ?: throw IllegalStateException("Save failure")
//                    }
//                    .also { states.playlist = it }
                        .also {
                            val newIdentifier =
                                it.id?.toIdentifier(LOCAL) ?: throw IllegalStateException("Save failure")
                            dispatch(Result.SetPlaylist(playlist = it, playlistIdentifier = newIdentifier))
                            executeRefresh(state = getState()) // hmmm will this work after dispatch
                        }
                        .also { updateView(getState()) }
                        //.also { onCommit?.onCommit(ObjectTypeDomain.PLAYLIST, listOf(it)) }
                        .also { publish(Label.AfterCommit(ObjectTypeDomain.PLAYLIST, listOf(it), intent?.afterCommit)) }
                }
            } else {
                throw IllegalStateException("Can't save non Memory playlist")
            }
        }

        private fun edit(intent: Intent.Edit, state: State) {
            state.playlistIdentifier
                .also {
                    recentLocalPlaylists.addRecentId(it.id as Long)
                    publish(
                        Label.Navigate(
                            NavigationModel(
                                Target.PLAYLIST_EDIT,
                                mapOf(SOURCE to it.source, PLAYLIST_ID to it.id)
                            ),
                            null
                        )
                    )
                }
        }

        private fun gotoPlaylist(intent: Intent.GotoPlaylist, state: State) {
            state.playlistItemDomain(intent.item)
                ?.playlistId
                ?.let {
                    publish(
                        Label.Navigate(
                            NavigationModel(
                                PLAYLIST,
                                mapOf(PLAYLIST_ID to it, Param.PLAY_NOW to false, SOURCE to LOCAL)
                            )
                        )
                    )
                }
        }

        private fun help(intent: Intent.Help, state: State) {
            publish(Label.Help)
        }

        private fun launch(intent: Intent.Launch, state: State) {
            state.playlist
                ?.platformId
                ?.also { publish(Label.LaunchPlaylist(it)) }
        }

        private fun moveItem(intent: Intent.Move, state: State) {
            dispatch(SetMoveState(state.dragFrom ?: intent.fromPosition, intent.toPosition))
        }

        private fun performMove(intent: Intent.MoveSwipe, getState: () -> State) {
            val thisState = getState()
            thisState.playlistItemDomain(intent.item)
                ?.also { itemDomain ->
                    thisState.playlist?.apply {
                        if (config.editableItems.not()) {
                            publish(Message(strings.getString(StringResource.playlist_error_please_add)))
                            updateView(state = thisState)
                        } else {
                            dispatch(SelectedPlaylistItem(itemDomain))
                            publish(
                                Label.ShowPlaylistsSelector(
                                    PlaylistsMviDialogContract.Config(
                                        strings.getString(StringResource.playlist_dialog_title_move),
                                        selectedPlaylists = setOf(thisState.playlist!!),
                                        multi = true,
                                        itemClick = { which: PlaylistDomain?, _ ->
                                            which
                                                ?.takeIf { it != PlaylistsMviDialogContract.ADD_PLAYLIST_DUMMY }
                                                ?.let { moveItemToPlaylist(it, getState()) }
                                                ?: run {
                                                    publish(Label.ShowPlaylistsCreator)
                                                }
                                        },
                                        confirm = { },
                                        dismiss = { publish(Label.ResetItemState) },
                                        suggestionsMedia = itemDomain.media,
                                        showPin = false,
                                    )
                                )
                            )
                        }
                    }
                }
        }

        private fun playlistSelected(intent: Intent.PlaylistSelected, state: State) {
            intent.playlist.id?.let { /*if (selected) */moveItemToPlaylist(intent.playlist, state) }
        }

        private fun moveItemToPlaylist(playlist: PlaylistDomain, state: State) {
            state.selectedPlaylistItem?.let { moveItem ->
                scope.launch {
                    moveItem
                        .takeIf {
                            playlistItemOrchestrator
                                .loadList(
                                    OrchestratorContract.Filter.PlatformIdListFilter(listOf(it.media.platformId)),
                                    LOCAL.flatOptions()
                                )
                                .filter { it.playlistId == playlist.id }.isEmpty()
                        }
                        ?.copy(playlistId = playlist.id!!)
                        ?.apply { dispatch(MovedPlaylistItem(this)) }
                        ?.copy(order = timeProvider.currentTimeMillis())
                        ?.apply { playlistItemOrchestrator.save(this, LOCAL.flatOptions()) }
                        ?.apply {
                            publish(
                                Label.ShowUndo(
                                    ItemMove,
                                    strings.getString(
                                        StringResource.playlist_item_moved_undo_message,
                                        listOf(playlist.title)
                                    )
                                )
                            )
                        }
                        ?.also { dispatch(SelectedPlaylistItem(null)) }
                        ?.also { dispatch(SetModified()) }
                        ?: apply {
                            publish(Error(strings.getString(StringResource.playlist_error_moveitem_already_exists)))
                        }
                }
            }
        }

        private fun commitMove(intent: Intent.CommitMove, state: State) {
            if (state.dragFrom != null && state.dragTo != null) {
                state.playlist
                    ?.let { playlist ->
                        playlistMutator.moveItem(playlist, state.dragFrom!!, state.dragTo!!)
                    }
                    ?.also { playlistModified ->
                        scope.launch {
                            state.dragTo
                                ?.let { playlistModified.items[it] }
                                ?.let { item ->
                                    item to (item.id ?: 0)
                                        .toIdentifier(state.playlistIdentifier.source)
                                        .flatOptions()
                                }
                                // updates order
                                ?.let { playlistItemOrchestrator.save(it.first, it.second) }
                                // fixme this works where id is there, for generated ids (share) need to find previous item in caches qnd reuse id
                                ?.also { dispatch(IdUpdate(it.id!!, it)) }
                                ?.also { dispatch(SetPlaylist(playlistModified)) }
                        }
                    }
            } else {
                if (state.dragFrom != null || state.dragTo != null) {
                    log.d("commitMove: Move failed .. ")
                    refresh(state = state)
                }
            }
            dispatch(SetMoveState(null, null))
        }

        private fun pause(intent: Intent.Pause, state: State) {

        }

        private fun play(intent: Intent.Play, state: State) {
//            if (isPlaylistPlaying()) {
//                chromeCastWrapper.killCurrentSession()
//            } else if (!canPlayPlaylist()) {
//                view.showError("Please add the playlist first")
//            } else {
            playUseCase.playLogic(state.playlist?.currentItemOrStart(), state.playlist, false)
//            }
        }

        private fun playItem(intent: Intent.PlayItem, state: State) {
            state.playlistItemDomain(intent.item)
                ?.let { itemDomain ->
                    if (state.isHeadless) {
                        publish(Label.PlayItem(playlistItem = itemDomain, start = intent.start))
                    } else if (!canPlayPlaylistItem(itemDomain)) {
                        //view.showError()
                        publish(Message(strings.getString(StringResource.playlist_error_please_add)))
                    } else {
                        playUseCase.playLogic(itemDomain, state.playlist, intent.start)
                    }
                }
        }

        private fun playModeChange(intent: Intent.PlayModeChange, state: State) {
            val mode = when (state.playlist?.mode) {
                PlaylistDomain.PlaylistModeDomain.SINGLE -> PlaylistDomain.PlaylistModeDomain.SHUFFLE
                PlaylistDomain.PlaylistModeDomain.SHUFFLE -> PlaylistDomain.PlaylistModeDomain.LOOP
                PlaylistDomain.PlaylistModeDomain.LOOP -> PlaylistDomain.PlaylistModeDomain.SINGLE
                else -> PlaylistDomain.PlaylistModeDomain.SINGLE
            }
            coroutines.mainScope.launch {
                state.playlist
                    ?.copy(mode = mode)
                    ?.apply { playlistOrchestrator.save(this, LOCAL.flatOptions()) }
            }
        }

        private fun relatedItems(intent: Intent.RelatedItem, state: State) {
            state.playlistItemDomain(intent.item)
                ?.also { item ->
                    multiPrefs.lastRemoteSearch = SearchRemoteDomain(
                        relatedToMediaPlatformId = item.media.platformId,
                        relatedToMediaTitle = item.media.title
                    )
                    multiPrefs.lastSearchType = SearchTypeDomain.REMOTE
                    publish(
                        Label.Navigate(
                            NavigationModel(PLAYLIST, mapOf(PLAYLIST_ID to YoutubeSearch.id, SOURCE to MEMORY))
                        )
                    )
                }
        }

        private fun resume(intent: Intent.Resume, state: State) {

        }

        private fun share(intent: Intent.Share, state: State) {
            state.playlist
                ?.also { publish(Label.Share(it)) }
        }

        private fun shareItem(intent: Intent.ShareItem, state: State) {
            state.playlistItemDomain(intent.item)
                ?.also { publish(Label.ShareItem(it)) }
        }

        private fun showCards(intent: Intent.ShowCards, state: State) {
            prefsWrapper.putBoolean(MultiPlatformPreferences.SHOW_VIDEO_CARDS, intent.isCards)
            coroutines.mainScope.launch {
                dispatch(SetCards(isCards = intent.isCards))
            }
        }

        private fun showChannel(intent: Intent.ShowChannel, state: State) {
            state.playlistItemDomain(intent.item)
                ?.takeIf { it.media.channelData.platformId != null }
                ?.also { publish(Label.LaunchChannel(it.media.channelData.platformId!!)) }
        }

        private fun showItem(intent: Intent.ShowItem, state: State) {
            state.playlistItemDomain(intent.item)
                ?.apply {
                    dispatch(SetFocusIndex(intent.item.index))
                    if (state.isHeadless) {
                        publish(Label.PlayItem(playlistItem = this))
                    } else {
                        val source =
                            if (state.playlist?.type != PlaylistDomain.PlaylistTypeDomain.APP) state.playlistIdentifier.source
                            else LOCAL
                        publish(Label.ShowItem(modelId = intent.item.id, item = this, source = source))
                    }
                }
        }

        private fun star(intent: Intent.Star, state: State) {
            coroutines.mainScope.launch {
                state.playlist
                    ?.copy(starred = state.playlist?.starred?.not() ?: false)
                    ?.apply { playlistOrchestrator.save(this, LOCAL.flatOptions()) }
            }
        }

        private fun starItem(intent: Intent.StarItem, state: State) {
            coroutines.mainScope.launch {
                state.playlistItemDomain(intent.item)
                    ?.takeIf { it.id != null }
                    ?.let { it.copy(media = it.media.copy(starred = !it.media.starred)) }
                    ?.also { playlistItemOrchestrator.save(it, LOCAL.deepOptions()) }
            }
        }

        private fun update(intent: Intent.Update, state: State) {
            scope.launch {
                publish(Label.Loading)
                try {
                    state.playlist
                        ?.takeIf { playlistUpdateUsecase.checkToUpdate(it) }
                        ?.let { playlistUpdateUsecase.update(it) }
                        ?.also {
                            if (it.success) {
                                publish(
                                    Message(
                                        strings.getString(playlist_items_updated, listOf(it.numberItems.toString()))
                                    )
                                )
                            } else {
                                publish(Error(strings.getString(playlist_error_updating, listOf(it.reason))))
                            }
                        }
                        ?.also { publish(Label.Loaded) }
                        ?: executeRefresh(state = state)
                } catch (e: Exception) {
                    log.e("Caught Error updating playlist", e)
                    publish(Label.Loaded)
                }
            }
        }

        private fun flowUpdatesMedia(intent: Intent.UpdatesMedia, state: State) {
            log.d("media changed: ${intent.op}, ${intent.source}, id=${intent.media.id} title=${intent.media.title}")
            when (intent.op) {
                FLAT,
                FULL -> state.playlist?.items
                    ?.find { it.media.platformId == intent.media.platformId }
                    ?.also { updatePlaylistItemMedia(null, intent.media, state) }

                DELETE -> Unit
            }
        }

        private fun flowUpdatesPlaylist(intent: Intent.UpdatesPlaylist, state: State) {
            log.d("playlist changed: ${intent.op}, ${intent.source}, id=${intent.plist.id} items=${intent.plist.items.size}")
            if (intent.plist.id?.toIdentifier(intent.source) == state.playlistIdentifier) {
                when (intent.op) {
                    FLAT ->
                        if (!intent.plist.matchesHeader(state.playlist)) {
                            state.playlist
                                ?.replaceHeader(intent.plist)
                                ?.apply { dispatch(SetPlaylist(this)) }
                        }

                    FULL ->
                        if (intent.plist != state.playlist) {
//                            state.playlist = intent.plist
//                            updateView()
                            dispatch(SetPlaylist(intent.plist))
                        }

                    DELETE -> {
//                        toastWrapper.show(res.getString(R.string.playlist_msg_deleted))
//                        view.exit()
                        // todo exit label? doesn't actually happen atm (maybe make delete button on playlist screen)
                    }
                }
            }
        }

        private fun flowUpdatesPlaylistItem(intent: Intent.UpdatesPlaylistItem, state: State) {
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
                ?.takeIf { coroutines.mainScopeActive }
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
                .also {
                    state.focusIndex?.apply {
                        publish(Label.ScrollToItem(this))
                        state.focusIndex = null
                    }
                }.also {
                    state.playlist
                        ?.currentIndex
                        ?.also {
                            publish(Label.HighlightPlayingItem(it))
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
                            val originalItemDomain = get(index)
                            val changedItemDomain =
                                plistItem ?: originalItemDomain.copy(media = media)
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
            dispatch(SetPlaylist(newPlaylist))
            dispatch(IdUpdate(modelId, changedItem))

            // todo check if this is needed
            mappedItem
                .takeIf { coroutines.mainScopeActive }
                ?.apply {
                    publish(Label.UpdateModelItem(this))
                }
        }

        private fun delete(intent: Intent.DeleteItem, state: State) {
            scope.launch {
                delay(400) // waits for ui animation
                log.d("delete item: ${intent.item.id} ${intent.item.title}")
                state.playlistItemDomain(intent.item)
                    ?.takeIf { it.id != null }
                    ?.also { log.d("found item ${it.id}") }
                    ?.let { deleteItem ->
                        dispatch(SetDeletedItem(deleteItem))
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

        private fun undo(intent: Intent.Undo, state: State) {
            when (intent.undoType) {
                ItemDelete ->
                    state.deletedPlaylistItem?.let { itemDomain ->
                        scope.launch {
                            playlistItemOrchestrator.save(itemDomain, LOCAL.deepOptions())
                            dispatch(SetDeletedItem(null))
                            executeRefresh(state = state)
                        }
                    }

                ItemMove ->
                    state.movedPlaylistItem
                        ?.copy(playlistId = state.playlistIdentifier.id!! as Long)
                        ?.also {
                            scope.launch {
                                playlistItemOrchestrator.save(
                                    it,
                                    state.playlistIdentifier.flatOptions()
                                )
                                dispatch(MovedPlaylistItem(null))
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

        // region utils
        private fun State.playlistItemDomain(itemModel: PlaylistItemMviContract.Model.Item) =
            itemsIdMap.get(itemModel.id)
        // fixme figure out if how cache models?
        //playlist?.items?.find { it.id == itemModel.id }

        private fun State.canPlayPlaylist() = (playlist?.id ?: 0) > 0

        private fun canPlayPlaylistItem(itemDomain: PlaylistItemDomain) =
            (itemDomain.playlistId ?: 0) > 0
        // endregion utils

        // region loadRefresh
        private fun setPlaylistData(intent: Intent.SetPlaylistData, getState: () -> State) {
            // fixme bit dodgy here - modifying state.
            log.d("setPlaylistData:" + intent.toString())
            val currentState = getState()
            coroutines.mainScope.launch {
                val notLoaded = currentState.playlist == null
                intent.plId
                    ?.takeIf { it != -1L }
                    ?.toIdentifier(intent.source)
                    ?.apply {
                        //state.playlistIdentifier = this
                        dispatch(SetPlaylistId(this))
                    }
                    ?.apply { executeRefresh(scrollToCurrent = notLoaded, state = getState()) }
                    ?.apply {
                        if (intent.playNow) {
                            queue.playNow(currentState.playlistIdentifier, intent.plItemId)
                        }
                    }
                    ?.apply {
                        getState().playlist?.also { recentLocalPlaylists.addRecent(it) }
                    }
                    ?: run {
                        if (dbInit.isInitialized()) {
                            //state.playlistIdentifier = prefsWrapper.lastViewedPlaylistId
                            dispatch(SetPlaylistId(prefsWrapper.lastViewedPlaylistId))
                            executeRefresh(scrollToCurrent = notLoaded, state = getState())
                        } else {
                            dbInit.addListener { b: Boolean ->
                                if (b) {
                                    dispatch(SetPlaylistId(3L.toIdentifier(LOCAL)))
                                    //state.playlistIdentifier = 3L.toIdentifier(LOCAL) // philosophy
                                    //updatePlaylist()
                                    refresh(state = getState())
                                }
                            }
                        }
                    }
            }
        }

        private fun refresh(state: State) {
            //log.e("refresh:" + state.playlistIdentifier.toString(), Exception("debug trace"))
            log.d("refresh:" + state.playlistIdentifier.toString())
            scope.launch { executeRefresh(state = state) }
        }

        // todo scroll to current might need default true
        private suspend fun executeRefresh(animate: Boolean = true, scrollToCurrent: Boolean = false, state: State) {
            //view.showRefresh()
            publish(Label.Loading)
            try {
                //log.e("executeRefresh:" + state.playlistIdentifier.toString(), Exception("debug trace"))
                log.d("executeRefresh: ${state.playlistIdentifier} focusIndex:${state.focusIndex}")
                val id = state.playlistIdentifier
                    .takeIf { it != OrchestratorContract.NO_PLAYLIST }
                    ?: prefsWrapper.currentPlayingPlaylistId
                val playlistOrDefault = playlistOrDefaultUsecase
                    .getPlaylistOrDefault(id)
                val playlistsTree = playlistOrchestrator
                    .loadList(AllFilter, LOCAL.flatOptions())
                    .buildTree()
                val focusIndex = if (scrollToCurrent && state.focusIndex == null) {
                    state.playlist?.currentIndex
                } else state.focusIndex
                dispatch(
                    Load(
                        playlist = playlistOrDefault?.first,
                        playlistIdentifier = playlistOrDefault?.first
                            ?.id?.toIdentifier(playlistOrDefault.second)
                            ?: throw IllegalStateException("Need an id"),
                        playlistsTree = playlistsTree,
                        playlistsTreeLookup = playlistsTree.buildLookup(),
                        focusIndex = focusIndex,
                        itemsIdMap = buildIdList(playlistOrDefault.first)
                    )
                )
                publish(Label.Loaded)
                focusIndex?.also { publish(Label.ScrollToItem(it)) }
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
            return itemsIdMap.also { map -> log.d(map.keys.associateBy { map[it]?.media?.title }.toString()) }
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
