package uk.co.sentinelweb.cuer.app.ui.playlist

import androidx.lifecycle.viewModelScope
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.AllFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.PlatformIdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.YoutubeSearch
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.LOCAL_PLAYER
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.NAV_DONE
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract.Companion.ADD_PLAYLIST_DUMMY
import uk.co.sentinelweb.cuer.app.ui.share.ShareContract
import uk.co.sentinelweb.cuer.app.usecase.AddPlaylistUsecase
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
import uk.co.sentinelweb.cuer.app.usecase.PlaylistOrDefaultUsecase
import uk.co.sentinelweb.cuer.app.usecase.PlaylistUpdateUsecase
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.SHOW_VIDEO_CARDS
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.app.util.share.AndroidShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.PlatformLaunchWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.PLAYLIST
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.ext.*
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator

class PlaylistPresenter(
    private val view: PlaylistContract.View,
    private val state: PlaylistContract.State,
    private val mediaOrchestrator: MediaOrchestrator,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val playlistUpdateUsecase: PlaylistUpdateUsecase,
    private val playlistOrDefaultUsecase: PlaylistOrDefaultUsecase,
    private val addPlaylistUsecase: AddPlaylistUsecase,
    private val modelMapper: PlaylistModelMapper,
    private val itemMapper: ItemModelMapper,
    private val queue: QueueMediatorContract.Producer,
    private val toastWrapper: ToastWrapper,
    private val ytCastContextHolder: ChromecastYouTubePlayerContextHolder,
    private val chromeCastWrapper: ChromeCastWrapper,
    private val ytJavaApi: PlatformLaunchWrapper,
    private val shareWrapper: AndroidShareWrapper,
    private val playlistMutator: PlaylistMutator,
    private val log: LogWrapper,
    private val timeProvider: TimeProvider,
    private val coroutines: CoroutineContextProvider,
    private val res: ResourceWrapper,
    private val dbInit: DatabaseInitializer,
    private val recentLocalPlaylists: RecentLocalPlaylists,
    private val playUseCase: PlayUseCase,
    private val multiPrefs: MultiPlatformPreferencesWrapper,
    private val appPlaylistInteractors: Map<Long, AppPlaylistInteractor>,
) : PlaylistContract.Presenter, PlaylistContract.External {

    override var interactions: PlaylistContract.Interactions? = null

    init {
        log.tag(this)
    }

    private fun isPlaylistPlaying() = isQueuedPlaylist && ytCastContextHolder.isConnected()
    private fun isPlaylistPinned() =
        state.playlist?.let { multiPrefs.pinnedPlaylistId == it.id } ?: false

    private val isQueuedPlaylist: Boolean
        get() = state.playlistIdentifier == queue.playlistId

    override val isCards: Boolean
        get() = multiPrefs.getBoolean(SHOW_VIDEO_CARDS, true) && !view.isHeadless

    private fun canPlayPlaylist() = (state.playlist?.id ?: 0) > 0

    private fun canPlayPlaylistItem(itemDomain: PlaylistItemDomain) =
        (itemDomain.playlistId ?: 0) > 0

    private val castConnectionListener = object : ChromecastConnectionListener {
        override fun onChromecastConnected(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
            if (isQueuedPlaylist) {
                view.setCastState(PlaylistContract.CastState.PLAYING)
            }
        }

        override fun onChromecastConnecting() {
            if (isQueuedPlaylist) {
                view.setCastState(PlaylistContract.CastState.CONNECTING)
            }
        }

        override fun onChromecastDisconnected() {
            if (isQueuedPlaylist) {
                view.setCastState(PlaylistContract.CastState.NOT_CONNECTED)
            }
        }
    }

    override fun onResume() {
        ytCastContextHolder.addConnectionListener(castConnectionListener)
        queue.currentPlaylistFlow
            .filter { isQueuedPlaylist }
            .onEach { log.d("q.playlist change id=${it.id} index=${it.currentIndex}") }
            .onEach { view.highlightPlayingItem(it.currentIndex) }
            .launchIn(coroutines.mainScope)
        listen()
    }

    override fun onPause() {
        ytCastContextHolder.removeConnectionListener(castConnectionListener)
        coroutines.cancel()
    }

    private fun listen() {
        playlistOrchestrator.updates
            .onEach { (op, source, plist) ->
                log.d("playlist changed: $op, $source, id=${plist.id} items=${plist.items.size}")
                if (plist.id?.toIdentifier(source) == state.playlistIdentifier) {
                    when (op) {
                        FLAT ->
                            if (!plist.matchesHeader(state.playlist)) {
                                state.playlist = state.playlist
                                    ?.replaceHeader(plist)
                                state.playlist
                                    ?.apply { updateHeader() }
                            }

                        FULL ->
                            if (plist != state.playlist) {
                                state.playlist = plist
                                updateView()
                            }

                        DELETE -> {
                            toastWrapper.show(res.getString(R.string.playlist_msg_deleted))
                            view.exit()
                        }
                    }
                }
            }
            .launchIn(coroutines.mainScope)

        playlistItemOrchestrator.updates
            .onEach { (op, source, plistItem) ->
                log.d("item changed: $op, $source, id=${plistItem.id} media=${plistItem.media.title}")
                val currentIndexBefore = state.playlist?.currentIndex
                when (op) { // todo just apply model updates (instead of full rebuild)
                    FLAT,
                    FULL,
                    -> if (plistItem.playlistId?.toIdentifier(source) == state.playlistIdentifier) {
                        state.playlist
                            ?.let { playlistMutator.addOrReplaceItem(it, plistItem) }
                            ?.takeIf { it != state.playlist }
                            ?.also { state.playlist = it }
                            ?.also { updateView() }
                    } else if (state.playlist?.type == APP) {// check to replace item in an app playlist
                        state.playlist
                            ?.items
                            ?.find { it.media.platformId == plistItem.media.platformId }
                            ?.also { updatePlaylistItemByMediaId(plistItem, plistItem.media) }
                            ?.let { state.playlist }
                    } else {
                        state.playlist
                            ?.let { playlistMutator.remove(it, plistItem) }
                            ?.takeIf { it != state.playlist }
                            ?.also { state.playlist = it }
                            ?.also { updateView() }
                    }

                    DELETE ->
                        state.playlist
                            ?.let { playlistMutator.remove(it, plistItem) }
                            ?.takeIf { it != state.playlist }
                            ?.also { state.playlist = it }
                            ?.also { updateView() }
                }.takeIf { !isQueuedPlaylist && currentIndexBefore != state.playlist?.currentIndex }
                    ?.apply {
                        state.playlist?.apply {
                            playlistOrDefaultUsecase.updateCurrentIndex(
                                this,
                                state.playlistIdentifier.flatOptions()
                            )
                        }
                    }
            }
            .launchIn(coroutines.mainScope)

        mediaOrchestrator.updates
            .onEach { (op, source, media) ->
                log.d("media changed: $op, $source, id=${media.id} title=${media.title}")
                when (op) {
                    FLAT,
                    FULL,
                    -> {
                        val containsMedia =
                            state.playlist?.items?.find { it.media.platformId == media.platformId } != null
                        if (containsMedia) {
                            updatePlaylistItemByMediaId(null, media)
                        }
                    }

                    DELETE -> Unit
                }
            }
            .launchIn(coroutines.mainScope)

        queue.currentItemFlow
            .onEach { item ->
                state.playlist?.items
                    ?.indexOfFirst { item?.id == it.id }
                    ?.also { view.highlightPlayingItem(it) }
            }
            .launchIn(coroutines.mainScope)
    }

    override fun initialise() {
        state.playlistIdentifier = multiPrefs.currentPlayingPlaylistId

    }

    override fun destroy() {
    }

    // move item to playlist
    override fun onItemSwipeRight(itemModel: PlaylistItemMviContract.Model.Item) { // move
        state.viewModelScope.launch {
            playlistItemDomain(itemModel)
                ?.also { itemDomain ->
                    state.playlist?.apply {
                        if (config.editableItems.not()) {
                            toastWrapper.show("Cant move the items before saving")
                            updateView()
                        } else {
                            state.selectedPlaylistItem = itemDomain
                            showPlaylistSelector()
                        }
                    }
                }
        }
    }

    private fun showPlaylistSelector() {
        view.showPlaylistSelector(
            PlaylistsMviDialogContract.Config(
                res.getString(R.string.playlist_dialog_title),
                selectedPlaylists = setOf(state.playlist!!),
                multi = true,
                itemClick = { which: PlaylistDomain?, _ ->
                    which
                        ?.takeIf { it != ADD_PLAYLIST_DUMMY }
                        ?.let { moveItemToPlaylist(it) }
                        ?: view.showPlaylistCreateDialog()
                },
                confirm = { },
                dismiss = { view.resetItemsState() },
                suggestionsMedia = state.selectedPlaylistItem?.media,
                showPin = false,
            )
        )
    }

    override fun onPlaylistSelected(playlist: PlaylistDomain, selected: Boolean) {
        playlist.id?.let { if (selected) moveItemToPlaylist(playlist) }
    }

    override fun onPlayModeChange(): Boolean {
        state.playlist?.copy(
            mode = when (state.playlist?.mode) {
                SINGLE -> SHUFFLE
                SHUFFLE -> LOOP
                LOOP -> SINGLE
                else -> SINGLE
            }
        )?.apply {
            commitHeaderChange(this)
        }
        return true
    }

    override fun onPlayPlaylist(): Boolean {
        if (isPlaylistPlaying()) {
            chromeCastWrapper.killCurrentSession()
        } else if (!canPlayPlaylist()) {
            view.showError("Please add the playlist first")
        } else {
            playUseCase.playLogic(state.playlist?.currentItemOrStart(), state.playlist, false)
        }
        return true
    }

    private fun moveItemToPlaylist(playlist: PlaylistDomain) {
        state.selectedPlaylistItem?.let { moveItem ->
            state.viewModelScope.launch {
                moveItem
                    .takeIf {
                        playlistItemOrchestrator
                            .loadList(
                                PlatformIdListFilter(listOf(it.media.platformId)),
                                LOCAL.flatOptions()
                            )
                            .filter { it.playlistId == playlist.id }.isEmpty()
                    }
                    ?.copy(playlistId = playlist.id!!)
                    ?.apply { state.movedPlaylistItem = this }
                    ?.copy(order = timeProvider.currentTimeMillis())
                    ?.apply { playlistItemOrchestrator.save(this, LOCAL.flatOptions()) }
                    ?.apply {
                        view.showUndo(
                            res.getString(
                                R.string.playlist_item_moved_undo_message,
                                playlist.title
                            ), ::undoMoveItem
                        )
                    }
                    ?.also { state.selectedPlaylistItem = null }
                    ?.also { state.isModified = true }
                    ?: apply {
                        view.showError(res.getString(R.string.playlist_error_moveitem_already_exists))
                    }
            }
        }
    }

    override fun undoMoveItem() {
        state.viewModelScope.launch {
            state.movedPlaylistItem
                ?.copy(playlistId = state.playlistIdentifier.id!! as Long)
                ?.apply {
                    playlistItemOrchestrator.save(
                        this,
                        state.playlistIdentifier.flatOptions()
                    )
                }
                ?.apply { state.movedPlaylistItem = null }
        }
    }

    override fun setAddPlaylistParent(id: Long) {
        state.addPlaylistParent = id
    }

    // done
    // delete item
    override fun onItemSwipeLeft(itemModel: PlaylistItemMviContract.Model.Item) {
        state.viewModelScope.launch {
            delay(400)

            playlistItemDomain(itemModel)
                ?.takeIf { it.id != null }
                ?.also { log.d("found item ${it.id}") }
                ?.let { deleteItem ->
                    state.deletedPlaylistItem = deleteItem
                    val appPlaylistInteractor = appPlaylistInteractors[state.playlist?.id]
                    val action = appPlaylistInteractor?.customResources?.customDelete?.label ?: "Deleted"
                    if (state.playlist?.type != APP
                        || !(appPlaylistInteractor?.hasCustomDeleteAction ?: false)
                    ) {
                        playlistItemOrchestrator.delete(deleteItem, LOCAL.flatOptions())
                    } else {
                        appPlaylistInteractor?.performCustomDeleteAction(deleteItem)
                        executeRefresh()
                    }
                    view.showUndo(
                        "$action: ${deleteItem.media.title}",
                        ::undoDelete
                    )
                }
        }
    }

    // done
    override fun undoDelete() {
        state.deletedPlaylistItem?.let { itemDomain ->
            state.viewModelScope.launch {
                playlistItemOrchestrator.save(itemDomain, LOCAL.deepOptions())
                state.deletedPlaylistItem = null
                executeRefresh()
            }
        }
    }

    override fun onItemViewClick(itemModel: PlaylistItemMviContract.Model.Item) {
        playlistItemDomain(itemModel)
            ?.apply {
                interactions
                    ?.onView(this)
                    ?: run {
                        state.focusIndex = itemModel.index
                        val source =
                            if (state.playlist?.type != APP) state.playlistIdentifier.source else LOCAL
                        view.showItemDescription(itemModel.id, this, source)
                    }
            }
    }

    override fun onItemPlayClicked(itemModel: PlaylistItemMviContract.Model.Item) {
        playlistItemDomain(itemModel)
            ?.let { itemDomain ->
                if (interactions != null) {
                    interactions?.onPlay(itemDomain)
                } else if (!canPlayPlaylistItem(itemDomain)) {
                    view.showError("Please add the playlist first")
                } else {
                    playUseCase.playLogic(itemDomain, state.playlist, false)
                }
            }
    }

    override fun onPlayStartClick(itemModel: PlaylistItemMviContract.Model.Item) {
        playlistItemDomain(itemModel)
            ?.let { itemDomain ->
                if (interactions != null) {
                    interactions?.onPlayStartClick(itemDomain)
                } else if (!canPlayPlaylistItem(itemDomain)) {
                    view.showError("Please add the playlist first")
                } else {
                    playUseCase.playLogic(itemDomain, state.playlist, false)
                }
            }
    }

    override fun onStarPlaylist(): Boolean {
        state.playlist
            ?.let { commitHeaderChange(it.copy(starred = !it.starred)) }
        return true
    }

    override fun onFilterNewItems(): Boolean {
        log.d("onFilterNewItems")
        return true
    }

    override fun onEdit(): Boolean {
        state.playlistIdentifier
            .apply { view.gotoEdit(this.id as Long, this.source) }
        return true
    }

    override fun onFilterPlaylistItems(): Boolean {
        log.d("onFilterPlaylistItems")
        return true
    }

    override fun onItemShowChannel(itemModel: PlaylistItemMviContract.Model.Item) {
        playlistItemDomain(itemModel)
            ?.takeUnless { ytJavaApi.launchChannel(it.media) }
            ?.also { toastWrapper.show("can't launch channel") }
            ?: toastWrapper.show("can't find video")
    }

    override fun onItemStar(itemModel: PlaylistItemMviContract.Model.Item) {
        state.viewModelScope.launch {
            playlistItemDomain(itemModel)
                ?.takeIf { it.id != null }
                ?.let { it.copy(media = it.media.copy(starred = !it.media.starred)) }
                ?.also { playlistItemOrchestrator.save(it, LOCAL.deepOptions()) }
        }
    }

    override fun onItemRelated(itemModel: PlaylistItemMviContract.Model.Item) {
        playlistItemDomain(itemModel)
            ?.also { item ->
                multiPrefs.lastRemoteSearch = SearchRemoteDomain(
                    relatedToMediaPlatformId = item.media.platformId,
                    relatedToMediaTitle = item.media.title
                )
                multiPrefs.lastSearchType = SearchTypeDomain.REMOTE

                view.navigate(
                    PlaylistContract.makeNav(YoutubeSearch.id, null, false, MEMORY)
                )
            }
    }

    override fun onItemShare(itemModel: PlaylistItemMviContract.Model.Item) {
        playlistItemDomain(itemModel)
            ?.let { itemDomain -> shareWrapper.share(itemDomain.media) }
    }

    override fun onItemGotoPlaylist(item: PlaylistItemMviContract.Model.Item) {
        playlistItemDomain(item)
            ?.playlistId
            ?.let {
                view.navigate(
                    NavigationModel(
                        NavigationModel.Target.PLAYLIST,
                        mapOf(PLAYLIST_ID to it, PLAY_NOW to false, SOURCE to LOCAL)
                    )
                )
            }
    }

    private fun commitHeaderChange(plist: PlaylistDomain) {
        state.viewModelScope.launch {
            playlistOrchestrator.save(plist, state.playlistIdentifier.flatOptions())
        }
    }

    override fun scroll(direction: PlaylistContract.ScrollDirection) {
        view.scrollTo(direction)
    }

    override fun onItemPlay(itemModel: PlaylistItemMviContract.Model.Item, external: Boolean) {
        playlistItemDomain(itemModel)
            ?.also {
                if (external) {
                    if (!ytJavaApi.launchVideo(it.media)) {
                        toastWrapper.show("can't launch video")
                    }
                } else {
                    if (interactions != null) {
                        interactions?.onPlay(it)
                    } else {
                        view.navigate(NavigationModel(LOCAL_PLAYER, mapOf(PLAYLIST_ITEM to it)))
                    }
                }
            }
            ?: toastWrapper.show("can't find video")
    }

    private fun playlistItemDomain(itemModel: PlaylistItemMviContract.Model.Item) = state.model
        ?.itemsIdMap
        ?.get(itemModel.id)

    override fun moveItem(fromPosition: Int, toPosition: Int) {
        if (state.dragFrom == null) {
            state.dragFrom = fromPosition
        }
        state.dragTo = toPosition
    }

    override fun commitMove() {
        if (state.dragFrom != null && state.dragTo != null) {
            state.playlist
                ?.let { playlist ->
                    playlistMutator.moveItem(playlist, state.dragFrom!!, state.dragTo!!)
                }
                ?.also { state.playlist = it }
                ?.also { playlistModified ->
                    state.viewModelScope.launch {
                        state.dragTo
                            ?.let { playlistModified.items[it] }
                            ?.let { item ->
                                item to (item.id ?: 0)
                                    .toIdentifier(state.playlistIdentifier.source)
                                    .flatOptions()
                            }
                            // updates order
                            ?.let { playlistItemOrchestrator.save(it.first, it.second) }
                    }
                }
                ?.also {
                    modelMapper.map(
                        domain = it,
                        isPlaying = isPlaylistPlaying(),
                        id = state.playlistIdentifier,
                        playlists = state.playlistsTreeLookup,
                        pinned = isPlaylistPinned(),
                        appPlaylist = state.playlist?.id?.let { appPlaylistInteractors[it] }
                    )
                        .also { view.setModel(it, false) }
                    view.highlightPlayingItem(it.currentIndex)
                }
        } else {
            if (state.dragFrom != null || state.dragTo != null) {
                log.d("commitMove: Move failed .. ")
                updatePlaylist()
            }
        }
        state.dragFrom = null
        state.dragTo = null
    }

    override fun setPlaylistData(plId: Long?, plItemId: Long?, playNow: Boolean, source: Source) {
        coroutines.mainScope.launch {
            val notLoaded = state.playlist == null
            plId
                ?.takeIf { it != -1L }
                ?.toIdentifier(source)
                ?.apply {
                    state.playlistIdentifier = this
                }
                ?.apply { executeRefresh(scrollToCurrent = notLoaded) }
                ?.apply {
                    if (playNow) {
                        queue.playNow(state.playlistIdentifier, plItemId)
                    }
                }
                ?.apply {
                    state.playlist?.also { recentLocalPlaylists.addRecent(it) }
                }
                ?: run {
                    if (dbInit.isInitialized()) {
                        state.playlistIdentifier = multiPrefs.lastViewedPlaylistId
                        executeRefresh(scrollToCurrent = notLoaded)
                    } else {
                        dbInit.addListener { b: Boolean ->
                            if (b) {
                                state.playlistIdentifier = 3L.toIdentifier(LOCAL) // philosophy
                                updatePlaylist()
                            }
                        }
                    }
                }
        }
    }

    override fun updatePlaylist() {
        state.viewModelScope.launch {
            view.showRefresh()
            try {
                state.playlist
                    ?.takeIf { playlistUpdateUsecase.checkToUpdate(it) }
                    ?.let { playlistUpdateUsecase.update(it) }
                    ?.also {
                        if (it.success) view.showMessage("${it.numberItems} new items")
                        else view.showError("Error updating ...")
                    }
                    ?.also { view.hideRefresh() }
                    ?: executeRefresh()
            } catch (e: Exception) {
                log.e("Caught Error updating playlist", e)
                view.showError(e.message ?: "Error updating ...")
            }
        }
    }

    override fun checkToSave() {
        if ((state.playlist?.id ?: 0) <= 0 && state.isModified) {
            view.showAlertDialog(modelMapper.mapSaveConfirmAlert(
                {
                    coroutines.mainScope.launch {
                        commitPlaylist()
                        view.navigate(NavigationModel(NAV_DONE))
                    }
                },
                { view.navigate(NavigationModel(NAV_DONE)) }
            ))
        } else {
            view.navigate(NavigationModel(NAV_DONE))
        }
    }

    override fun onShowCards(cards: Boolean): Boolean {
        multiPrefs.putBoolean(SHOW_VIDEO_CARDS, cards)
        coroutines.mainScope.launch {
            state.focusIndex = view.getScrollIndex()
            view.newAdapter()
            executeRefresh(false)
        }
        return true
    }

    override fun onHelp() {
        view.showHelp()
    }

    override suspend fun commitPlaylist(onCommit: ShareContract.Committer.OnCommit?) {
        if (state.playlistIdentifier.source == MEMORY) {
            log.i("commitPlaylist: id:${state.playlistIdentifier}")
            addPlaylistUsecase
                .addPlaylist(state.playlist!!, state.addPlaylistParent)
                .also {
                    state.playlistIdentifier =
                        it.id?.toIdentifier(LOCAL) ?: throw IllegalStateException("Save failure")
                }
                .also { state.playlist = it }
                .also { updateView() }
                .also { onCommit?.onCommit(PLAYLIST, listOf(it)) }
        } else {
            throw IllegalStateException("Can't save non Memory playlist")
        }
    }

    override fun reloadHeader() {
        coroutines.mainScope.launch { updateHeader() }
    }

    private suspend fun executeRefresh(animate: Boolean = true, scrollToCurrent: Boolean = false) {
        view.showRefresh()
        try {
//            log.e(
//                "executeRefresh: ${state.playlistIdentifier.id}, animate:$animate, scrollToCurrent:$scrollToCurrent",
//                Exception()
//            )
            playlistOrDefaultUsecase
                .getPlaylistOrDefault(
                    state.playlistIdentifier.id as Long,
                    state.playlistIdentifier.source.flatOptions()
                )
                .also { state.playlist = it?.first }
                ?.also { (playlist, source) ->
                    playlist.id
                        ?.also { id -> state.playlistIdentifier = id.toIdentifier(source) }
                        ?: throw IllegalStateException("Need an id")
                }
                ?.also {
                    state.playlistsTree = playlistOrchestrator
                        .loadList(AllFilter, LOCAL.flatOptions())
                        .buildTree()
                        .also {
                            state.playlistsTreeLookup = it.buildLookup()
                        }
                }
                ?.also {
                    if (it.second == LOCAL || it.first.type == APP) {
                        multiPrefs.lastViewedPlaylistId =
                            state.playlistIdentifier as OrchestratorContract.Identifier<Long>
                    }
                }
                .also {
                    if (scrollToCurrent && state.focusIndex == null) {
                        state.focusIndex = state.playlist?.currentIndex
                    }
                    updateView(animate)
                }
        } catch (e: Throwable) {
            log.e("Error loading playlist", e)
            view.showError("Load failed: ${e::class.java.simpleName}")
            view.hideRefresh()
        }
    }

    private suspend fun updateView(animate: Boolean = true) = withContext(coroutines.Main) {
        state.playlist
            .takeIf { coroutines.mainScopeActive }
            ?.let {
                modelMapper.map(
                    domain = it,
                    isPlaying = isPlaylistPlaying(),
                    id = state.playlistIdentifier,
                    playlists = state.playlistsTreeLookup,
                    pinned = isPlaylistPinned(),
                    appPlaylist = state.playlist?.id?.let { appPlaylistInteractors[it] }
                )
            }
            ?.also { state.model = it }
            ?.also { view.setModel(it, animate) }
            .also {
                state.focusIndex?.apply {
                    view.scrollToItem(this)
                    state.focusIndex = null
                }
            }.also {
                state.playlist
                    ?.currentIndex
                    ?.also { view.highlightPlayingItem(it) }
            }
    }


    private suspend fun updateHeader() = withContext(coroutines.Main) {
        state.playlist
            .takeIf { coroutines.mainScopeActive }
            ?.apply {
                view.setHeaderModel(
                    modelMapper.map(
                        this,
                        isPlaylistPlaying(),
                        false,
                        id = state.playlistIdentifier,
                        playlists = state.playlistsTreeLookup,
                        pinned = isPlaylistPinned(),
                        appPlaylist = state.playlist?.id?.let { appPlaylistInteractors[it] }
                    )
                )
                state.playlist?.currentIndex?.also {
                    view.highlightPlayingItem(it)
                }
            }
    }

    private fun updatePlaylistItemByMediaId(plistItem: PlaylistItemDomain?, media: MediaDomain) {
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
                            model.itemsIdMap.entries.firstOrNull {
                                if (originalItemDomain.id != null) {
                                    it.value.id == originalItemDomain.id
                                } else {
                                    it.value == originalItemDomain
                                }
                            }?.key
                                ?.also { updateItem(index, it, changedItemDomain) }
                                ?: throw Exception("Couldn't lookup model ID for $originalItemDomain keys=${model.itemsIdMap.keys}")
                        }
                    }
            }
    }

    private fun updateItem(
        index: Int,
        modelId: Long,
        changedItem: PlaylistItemDomain,
    ) {
        state.playlist = state.playlist?.let {
            it.copy(items = it.items.toMutableList().apply { set(index, changedItem) })
        }

        val mappedItem = itemMapper.mapItem(
            modelId, changedItem, index,
            state.playlist?.config?.editableItems ?: false,
            state.playlist?.config?.deletableItems ?: false,
            state.playlist?.config?.editable ?: false,
            playlistText = modelMapper.mapPlaylistText(
                changedItem,
                state.playlist,
                state.playlistsTreeLookup
            ),
            showOverflow = true,
            deleteResources = state.playlist?.id?.let { appPlaylistInteractors[it] }?.customResources?.customDelete
        )
        state.model = state.model?.let {
            it.copy(items = it.items?.toMutableList()?.apply { set(index, mappedItem) })
        }?.also { it.itemsIdMap[modelId] = changedItem }

        mappedItem
            .takeIf { coroutines.mainScopeActive }
            ?.apply { view.updateItemModel(this) }
    }

}