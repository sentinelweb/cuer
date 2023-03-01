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
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Companion.NO_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.AllFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.PlatformIdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.*
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
import uk.co.sentinelweb.cuer.app.usecase.*
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.app.util.wrapper.PlatformLaunchWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ShareWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.ext.*
import uk.co.sentinelweb.cuer.domain.mappers.PlaylistAndItemMapper
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator

class PlaylistMviStoreFactory(
    private val storeFactory: StoreFactory,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val playlistUpdateUsecase: PlaylistUpdateUsecase,
    private val playlistOrDefaultUsecase: PlaylistOrDefaultUsecase,
    private val prefsWrapper: MultiPlatformPreferencesWrapper,
    private val dbInit: DatabaseInitializer,
    private val recentLocalPlaylists: RecentLocalPlaylists,
    private val queue: QueueMediatorContract.Producer,
    private val appPlaylistInteractors: Map<Identifier<GUID>, AppPlaylistInteractor>,
    private val playlistMutator: PlaylistMutator,
    private val util: PlaylistMviUtil,
    private val modelMapper: PlaylistMviModelMapper,
    private val itemModelMapper: PlaylistMviItemModelMapper,
    private val playUseCase: PlayUseCase,
    private val strings: StringDecoder,
    private val timeProvider: TimeProvider,
    private val addPlaylistUsecase: AddPlaylistUsecase,
    private val multiPrefs: MultiPlatformPreferencesWrapper,
    private val idGenerator: IdGenerator,
    private val shareWrapper: ShareWrapper,
    private val platformLauncher: PlatformLaunchWrapper,
    private val paiMapper: PlaylistAndItemMapper,
    private val mediaUpdateFromPlatformUseCase: MediaUpdateFromPlatformUseCase,
) {
    init {
        log.tag(this)
        log.d("init PlaylistMviStoreFactory")
    }

    private sealed class Result {
        data class Load(
            var playlistIdentifier: Identifier<GUID>,
            var playlist: PlaylistDomain?,
            var playlistsTree: PlaylistTreeDomain?,
            var playlistsTreeLookup: Map<Identifier<GUID>, PlaylistTreeDomain>?,
//            val itemsIdMap: MutableMap<Long, PlaylistItemDomain>,
        ) : Result()

        data class SetDeletedItem(val item: PlaylistItemDomain?) : Result()
        data class SetPlaylist(
            val playlist: PlaylistDomain?,
            var playlistIdentifier: Identifier<GUID>? = null
        ) : Result()

        data class SetPlaylistId(
            var playlistIdentifier: Identifier<GUID>
        ) : Result()

        data class IdUpdate(val modelId: Identifier<GUID>, val changedItem: PlaylistItemDomain) : Result()
        data class SelectedPlaylistItem(val item: PlaylistItemDomain?) : Result()
        data class MovedPlaylistItem(val item: PlaylistItemDomain?) : Result()
        data class SetModified(val modified: Boolean = true) : Result()
        data class SetMoveState(val from: Int?, val to: Int?) : Result()
        data class SetCards(val isCards: Boolean) : Result()
        data class SetFocusIndex(val index: Int?) : Result()
        data class SetHeadless(val headless: Boolean) : Result()
    }

    private sealed class Action {
        object Init : Action()
    }

    private class ReducerImpl(private val idGenerator: IdGenerator) : Reducer<State, Result> {
        override fun State.reduce(msg: Result): State =
            when (msg) {
                is Load -> buildIdList(msg.playlist, this).let {
                    copy(
                        playlistIdentifier = msg.playlistIdentifier,
                        playlist = msg.playlist,
                        playlistsTree = msg.playlistsTree,
                        playlistsTreeLookup = msg.playlistsTreeLookup,
                        itemsIdMap = it,
                        itemsIdMapReversed = it.reverseLookup()
                    )
                }

                is SetDeletedItem -> copy(deletedPlaylistItem = msg.item)
                is SetPlaylist -> buildIdList(msg.playlist, this).let {
                    copy(
                        playlist = msg.playlist,
                        playlistIdentifier = msg.playlistIdentifier ?: playlistIdentifier,
                        itemsIdMap = it,
                        itemsIdMapReversed = it.reverseLookup()
                    )
                }

                is IdUpdate -> copy(
                    itemsIdMap = itemsIdMap.apply { put(msg.modelId, msg.changedItem) },
                    itemsIdMapReversed = itemsIdMapReversed.apply { put(msg.changedItem, msg.modelId) }
                )

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

        private fun buildIdList(
            domain: PlaylistDomain?,
            state: State
        ): MutableMap<Identifier<GUID>, PlaylistItemDomain> {
            val existingReverseLookup = state.itemsIdMapReversed
            val itemsIdMap = mutableMapOf<Identifier<GUID>, PlaylistItemDomain>()
            domain?.items?.mapIndexed { index, item ->
                val modelId = item.id ?: existingReverseLookup[item] ?: idGenerator.value
                itemsIdMap[modelId] = item
            }
            return itemsIdMap
        }

        private fun MutableMap<Identifier<GUID>, PlaylistItemDomain>.reverseLookup() =
            this.let { m -> m.keys.associateBy { m[it]!! } }.toMutableMap()
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

        override fun executeIntent(intent: Intent, getState: () -> State) = intent
            //.also { log.d(it.toString()) }
            .let {
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
            }

        private fun headless(intent: Intent, state: State) {
            dispatch(SetHeadless(true))
        }

        private fun flowQueuePlaylist(intent: Intent.QueuePlaylistFlow, state: State) {
            if (state.playlistIdentifier == queue.playlistId) {
                intent.item?.currentIndex
                    ?.takeIf { it > 0 && it < (state.playlist?.items?.size ?: 0) }
                    ?.let { state.playlist?.items?.get(it) }
                    ?.id
                    ?.also { publish(Label.HighlightPlayingItem(it)) }
            }
        }

        private fun flowQueueItem(intent: Intent.QueueItemFlow, state: State) {
            state.playlist?.items
                ?.find { intent.item?.id == it.id }
                ?.id
                ?.also { publish(Label.HighlightPlayingItem(it)) }
        }

        private fun checkToSave(intent: Intent.CheckToSave, state: State) {
            if (state.playlist?.id?.source == MEMORY || state.isModified) {
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
                        .also {
                            val newIdentifier =
                                it.id ?: throw IllegalStateException("Save failure")
                            dispatch(SetPlaylist(playlist = it, playlistIdentifier = newIdentifier))
                            //log.d("call executeRefresh: commit: no params")
                            executeRefresh(state = getState()) // hmmm will this work after dispatch
                        }
                        .also { publish(Label.AfterCommit(ObjectTypeDomain.PLAYLIST, listOf(it), intent?.afterCommit)) }
                }
            } else {
                throw IllegalStateException("Can't save non Memory playlist")
            }
        }

        private fun edit(intent: Intent.Edit, state: State) {
            state.playlistIdentifier
                .also {
                    recentLocalPlaylists.addRecentId(it.id)
                    publish(
                        Label.Navigate(
                            NavigationModel(
                                Target.PLAYLIST_EDIT,
                                mapOf(SOURCE to it.source, PLAYLIST_ID to it.id.value)
                            ), null
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
                                mapOf(PLAYLIST_ID to it.id.value, Param.PLAY_NOW to false, SOURCE to LOCAL)
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
                ?.also { platformLauncher.launchPlaylist(it) }
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
                            publish(Message(strings.get(StringResource.playlist_error_please_add)))
                        } else {
                            dispatch(SelectedPlaylistItem(itemDomain))
                            publish(
                                Label.ShowPlaylistsSelector(
                                    PlaylistsMviDialogContract.Config(
                                        strings.get(StringResource.playlist_dialog_title_move),
                                        selectedPlaylists = setOf(thisState.playlist!!),
                                        multi = true,
                                        itemClick = { which: PlaylistDomain?, _ ->
                                            which
                                                ?.takeIf { it != PlaylistsMviDialogContract.ADD_PLAYLIST_DUMMY }
                                                ?.let { moveItemToPlaylist(it, getState()) }
                                                ?: run { publish(Label.ShowPlaylistsCreator) }
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
            intent.playlist.id?.let { moveItemToPlaylist(intent.playlist, state) }
        }

        private fun moveItemToPlaylist(playlist: PlaylistDomain, state: State) {
            state.selectedPlaylistItem?.let { moveItem ->
                scope.launch {
                    moveItem
                        .takeIf {
                            playlistItemOrchestrator
                                .loadList(PlatformIdListFilter(listOf(it.media.platformId)), LOCAL.flatOptions())
                                .filter { it.playlistId == playlist.id }
                                .isEmpty()
                        }
                        ?.copy(playlistId = playlist.id!!)
                        ?.apply { dispatch(MovedPlaylistItem(this)) } // fixme why so early
                        ?.copy(order = timeProvider.currentTimeMillis())
                        ?.let { item -> mediaUpdateFromPlatformUseCase.checkToUpdateItem(item) }
                        // ?.also{log.d("media.duration: ${it.media.duration}")}
                        ?.apply { playlistItemOrchestrator.save(this, LOCAL.deepOptions()) }
                        //?.also { updatePlaylistItemMedia(it, it.media, state) }
                        //?.apply { playlistItemOrchestrator.save(this, LOCAL.flatOptions()) }
                        ?.apply {
                            publish(
                                Label.ShowUndo(
                                    ItemMove,
                                    strings.get(
                                        StringResource.playlist_item_moved_undo_message,
                                        listOf(playlist.title)
                                    )
                                )
                            )
                        }
                        ?.also { recentLocalPlaylists.addRecent(playlist) }
//                        ?.also { dispatch(SelectedPlaylistItem(null)) }
//                        ?.also { dispatch(SetModified()) }
                        ?.also { executeRefresh(animate = true, state = state) }
                        ?: apply { publish(Error(strings.get(StringResource.playlist_error_moveitem_already_exists))) }
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
                                    item to item.id!!.flatOptions()
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
            state.playlist
                ?.takeIf { it.items.isNotEmpty() }
                ?.also { pl -> playUseCase.playLogic(paiMapper.map(pl, pl.currentItemOrStart()!!), false) }
        }

        private fun playItem(intent: Intent.PlayItem, state: State) {
            state.playlistItemDomain(intent.item)
                ?.let { itemDomain ->
                    if (state.isHeadless) {
                        publish(Label.PlayItem(playlistItem = itemDomain, start = intent.start))
                    } else if (!canPlayPlaylistItem(itemDomain)) {
                        publish(Message(strings.get(StringResource.playlist_error_please_add)))
                    } else if (itemDomain.media.isLiveBroadcastUpcoming) {
                        publish(Message(strings.get(StringResource.playlist_error_upcoming)))
                    } else {
                        playUseCase.playLogic(paiMapper.map(itemDomain.playlistId!!, itemDomain), intent.start)
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
                            NavigationModel(PLAYLIST, mapOf(PLAYLIST_ID to YoutubeSearch.id.value, SOURCE to MEMORY))
                        )
                    )
                }
        }

        private fun resume(intent: Intent.Resume, state: State) {

        }

        private fun share(intent: Intent.Share, state: State) {
            state.playlist
                ?.also { shareWrapper.share(it) }
        }

        private fun shareItem(intent: Intent.ShareItem, state: State) {
            state.playlistItemDomain(intent.item)
                ?.also { shareWrapper.share(it.media) }
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
                ?.also { platformLauncher.launchChannel(it.media.channelData.platformId!!) }
        }

        private fun showItem(intent: Intent.ShowItem, state: State) {
            state.playlistItemDomain(intent.item)
                ?.apply {
                    dispatch(SetFocusIndex(intent.item.index))
                    if (state.isHeadless) {
                        publish(Label.PlayItem(playlistItem = this))
                    } else {
                        publish(Label.ShowItem(modelId = intent.item.id, item = this))
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
                                        strings.get(playlist_items_updated, listOf(it.numberItems.toString()))
                                    )
                                )
                            } else {
                                publish(Error(strings.get(playlist_error_updating, listOf(it.reason))))
                            }
                        }
                        ?.also { publish(Label.Loaded) }
                        ?: run {
                            //log.d("call executeRefresh: update: no params")
                            executeRefresh(state = state)
                        }
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
            if (intent.plist.id == state.playlistIdentifier) {
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
                -> if (intent.item.playlistId == state.playlistIdentifier) {
                    state.playlist
                        ?.let { playlistMutator.addOrReplaceItem(it, intent.item) }
                        ?.takeIf { it != state.playlist }
                        ?.also { dispatch(SetPlaylist(it)) }
                } else if (state.playlist?.type == PlaylistDomain.PlaylistTypeDomain.APP) {// check to replace item in an app playlist
                    state.playlist
                        ?.items
                        ?.find { it.media.platformId == intent.item.media.platformId }
                        ?.also { updatePlaylistItemMedia(intent.item, intent.item.media, state) }
                    //?.let { state.playlist }
                } else {
                    state.playlist
                        ?.let { playlistMutator.remove(it, intent.item) }
                        ?.takeIf { it != state.playlist }
                        ?.also { dispatch(SetPlaylist(it)) }
                }

                DELETE ->
                    state.playlist
                        ?.let { playlistMutator.remove(it, intent.item) }
                        ?.takeIf { it != state.playlist }
                        ?.also { dispatch(SetPlaylist(it)) }
            }.takeIf { !util.isQueuedPlaylist(state) && currentIndexBefore != state.playlist?.currentIndex }
                ?.apply {
                    scope.launch {
                        state.playlist?.apply {
                            playlistOrDefaultUsecase.updateCurrentIndex(this, state.playlistIdentifier.flatOptions())
                        }
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
            modelId: Identifier<GUID>,
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
                ?.apply { publish(Label.UpdateModelItem(this)) }
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
                        if (state.playlist?.type == PlaylistDomain.PlaylistTypeDomain.APP
                            && (appPlaylistInteractor?.hasCustomDeleteAction ?: false)
                        ) {
                            appPlaylistInteractor?.performCustomDeleteAction(deleteItem)
                            //log.d("call executeRefresh: delete: no params")
                            executeRefresh(state = state)
                        } else {
                            log.d("deleting item: ${intent.item.id}")
                            playlistItemOrchestrator.delete(deleteItem, LOCAL.flatOptions())
                            log.d("deleted item: ${intent.item.id}")
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
                        ?.copy(playlistId = state.playlistIdentifier)
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
        // endregion

        // region utils
        private fun State.playlistItemDomain(itemModel: PlaylistItemMviContract.Model.Item) =
            itemsIdMap.get(itemModel.id)

        private fun canPlayPlaylistItem(itemDomain: PlaylistItemDomain) =
            itemDomain.media.id?.source == LOCAL//|| itemDomain.id?.source == MEMORY || itemDomain.id?.source == LOCAL_NETWORK
        // endregion utils

        // region loadRefresh
        private fun setPlaylistData(intent: Intent.SetPlaylistData, getState: () -> State) {
            log.d("setPlaylistData:" + intent.toString())
            val currentState = getState()
            coroutines.mainScope.launch {
                val notLoaded = currentState.playlist == null
                //log.d("setPlaylistData: notLoaded: $notLoaded")
                intent.plId
                    ?.takeIf { it != NO_PLAYLIST.id }
                    ?.toIdentifier(intent.source)
                    ?.apply { executeRefresh(state = getState(), scrollToCurrent = notLoaded, id = this) }
                    ?.apply {
                        if (intent.playNow) {
                            queue.playNow(this, intent.plItemId?.toIdentifier(intent.source))
                        }
                    }
                    ?.apply { recentLocalPlaylists.addRecentId(intent.plId) }
                    ?: run {
                        if (dbInit.isInitialized()) {
                            //log.d("call executeRefresh: setPlaylistData.default: id = ${prefsWrapper.lastViewedPlaylistId}")
                            executeRefresh(
                                state = getState(),
                                scrollToCurrent = true,
                                id = prefsWrapper.lastViewedPlaylistId
                            )
                        } else {
                            dbInit.addListener { success: Boolean ->
                                if (success) {
                                    refresh(state = getState())
                                }
                            }
                        }
                    }
            }
        }

        private fun refresh(state: State) {
            val notLoaded = state.playlist == null
            log.d("refresh: notLoaded: $notLoaded")
            scope.launch {
                //log.d("call executeRefresh: refresh: id = ${prefsWrapper.lastViewedPlaylistId}")
                executeRefresh(state = state, scrollToCurrent = notLoaded, id = prefsWrapper.lastViewedPlaylistId)
            }
        }

        private suspend fun executeRefresh(
            animate: Boolean = true,
            scrollToCurrent: Boolean = false,
            state: State,
            id: Identifier<GUID>? = null
        ) {
            publish(Label.Loading)
            try {
                log.d("executeRefresh: state.id: ${state.playlistIdentifier} scrollToCurrent: $scrollToCurrent focusIndex: ${state.focusIndex} id: $id")
                val loadId =
                    id
                        ?: state.playlistIdentifier.takeIf { it != NO_PLAYLIST }
                        ?: prefsWrapper.lastViewedPlaylistId.takeIf { it != NO_PLAYLIST }
                        ?: prefsWrapper.currentPlayingPlaylistId

                log.d("executeRefresh: loadId: $loadId")
                val playlistOrDefault = playlistOrDefaultUsecase.getPlaylistOrDefault(loadId)
                val playlistsTree = playlistOrchestrator
                    .loadList(AllFilter, LOCAL.flatOptions())
                    .buildTree()
                val focusIndex = if (scrollToCurrent && state.focusIndex == null) {
                    playlistOrDefault?.currentIndex
                } else state.focusIndex
                dispatch(
                    Load(
                        playlist = playlistOrDefault,
                        playlistIdentifier = playlistOrDefault?.id
                            ?: throw IllegalStateException("Need an id"),
                        playlistsTree = playlistsTree,
                        playlistsTreeLookup = playlistsTree.buildLookup(),
                    )
                )
                publish(Label.Loaded)
                focusIndex
                    ?.also { publish(Label.ScrollToItem(it)) }
                    ?.also { dispatch(SetFocusIndex(null)) }
                prefsWrapper.lastViewedPlaylistId = loadId
                //log.d("loaded:lastViewedPlaylistId: ${prefsWrapper.lastViewedPlaylistId}")
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
                reducer = ReducerImpl(idGenerator)
            ) {}
}
