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
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Companion.NO_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Options
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.REMOTE_SEARCH_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.util.PlaylistMediaLookupOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.util.PlaylistOrDefaultOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.util.PlaylistUpdateOrchestrator
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLISTS_FRAGMENT
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST_FRAGMENT
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.search.SearchContract.SearchType.REMOTE
import uk.co.sentinelweb.cuer.app.ui.share.ShareContract
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.*
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.PLAYLIST
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.SearchRemoteDomain
import uk.co.sentinelweb.cuer.domain.ext.*
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator

class PlaylistPresenter(
    private val view: PlaylistContract.View,
    private val state: PlaylistContract.State,
    private val mediaOrchestrator: MediaOrchestrator,
    private val playlistMediaLookupOrchestrator: PlaylistMediaLookupOrchestrator,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val playlistUpdateOrchestrator: PlaylistUpdateOrchestrator,
    private val playlistOrDefaultOrchestrator: PlaylistOrDefaultOrchestrator,
    private val modelMapper: PlaylistModelMapper,
    private val queue: QueueMediatorContract.Producer,
    private val toastWrapper: ToastWrapper,
    private val ytCastContextHolder: ChromecastYouTubePlayerContextHolder,
    private val chromeCastWrapper: ChromeCastWrapper,
    private val ytJavaApi: YoutubeJavaApiWrapper,
    private val shareWrapper: ShareWrapper,
    private val playlistMutator: PlaylistMutator,
    private val prefsWrapper: GeneralPreferencesWrapper,
    private val log: LogWrapper,
    private val timeProvider: TimeProvider,
    private val coroutines: CoroutineContextProvider,
    private val res: ResourceWrapper,
    private val dbInit: DatabaseInitializer,
) : PlaylistContract.Presenter, PlaylistContract.External {

    override var interactions: PlaylistContract.Interactions? = null

    init {
        log.tag(this)
    }

    private fun isPlaylistPlaying() = isQueuedPlaylist && ytCastContextHolder.isConnected()
    private fun isPlaylistPinned() = state.playlist?.let { prefsWrapper.getLong(PINNED_PLAYLIST, 0) == it.id } ?: false

    private val isQueuedPlaylist: Boolean
        get() = state.playlistIdentifier == queue.playlistId

    private val castConnectionListener = object : ChromecastConnectionListener {
        override fun onChromecastConnected(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
            if (isQueuedPlaylist) {
                view.setPlayState(PlaylistContract.PlayState.PLAYING)
            }
        }

        override fun onChromecastConnecting() {
            if (isQueuedPlaylist) {
                view.setPlayState(PlaylistContract.PlayState.CONNECTING)
            }
        }

        override fun onChromecastDisconnected() {
            if (isQueuedPlaylist) {
                view.setPlayState(PlaylistContract.PlayState.NOT_CONNECTED)
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
                            view.exit()// todo exit or back
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
                            playlistOrDefaultOrchestrator.updateCurrentIndex(this, state.playlistIdentifier.flatOptions())
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
                        val containsMedia = state.playlist?.items?.find { it.media.platformId == media.platformId } != null
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
        state.playlistIdentifier = prefsWrapper.getPair(CURRENT_PLAYLIST, NO_PLAYLIST.toPair()).toIdentifier()

    }

    override fun destroy() {
    }

    // move item to playlist
    override fun onItemSwipeRight(itemModel: ItemContract.Model) { // move
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
            PlaylistsDialogContract.Config(
                selectedPlaylists = setOf(state.playlist!!),
                multi = true,
                itemClick = { which: PlaylistDomain?, _ ->
                    which
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
        } else {
            // todo local play option?
            state.playlist?.let {
                coroutines.computationScope.launch {
                    it.id?.apply { queue.switchToPlaylist(state.playlistIdentifier) }
                    it.currentItemOrStart()
                        ?.let { queue.onItemSelected(it, forcePlay = true, resetPosition = false) }
                        ?: toastWrapper.show("No items to play")
                }
            }
            if (!ytCastContextHolder.isConnected()) {
                view.showCastRouteSelectorDialog()
            }
        }

        return true
    }

    private fun moveItemToPlaylist(playlist: PlaylistDomain) {
        state.selectedPlaylistItem
            ?.let { moveItem ->
                state.viewModelScope.launch {
                    moveItem
                        .copy(playlistId = playlist.id!!)
                        .apply { state.movedPlaylistItem = this }
                        .copy(order = timeProvider.currentTimeMillis())
                        .apply { playlistItemOrchestrator.save(this, LOCAL.flatOptions()) }
                        .apply { view.showUndo("Moved to : ${playlist.title}", ::undoMoveItem) }
                        .also { state.selectedPlaylistItem = null }
                }
            }
    }

    override fun undoMoveItem() {
        state.viewModelScope.launch {
            state.movedPlaylistItem
                ?.copy(playlistId = state.playlistIdentifier.id!! as Long)
                ?.apply { playlistItemOrchestrator.save(this, state.playlistIdentifier.flatOptions()) }
                ?.apply { state.movedPlaylistItem = null }
        }
    }

    override fun setAddPlaylistParent(id: Long) {
        state.addPlaylistParent = id
    }

    // delete item
    override fun onItemSwipeLeft(itemModel: ItemContract.Model) {
        state.viewModelScope.launch {
            delay(400)

            playlistItemDomain(itemModel)
                ?.takeIf { it.id != null }
                ?.also { log.d("found item ${it.id}") }
                ?.let { deleteItem ->
                    state.deletedPlaylistItem = deleteItem
                    playlistItemOrchestrator.delete(deleteItem, LOCAL.flatOptions())
                    view.showUndo("Deleted: ${deleteItem.media.title}", ::undoDelete) // todo extract
                }
        }
    }

    override fun onItemViewClick(itemModel: ItemContract.Model) {
        playlistItemDomain(itemModel)
            ?.apply {
                interactions
                    ?.onView(this)
                    ?: run {
                        val source = if (state.playlist?.type != APP) state.playlistIdentifier.source else LOCAL
                        view.showItemDescription(itemModel.id, this, source)
                    }
            } // todo pass identifier?
    }

    override fun onItemClicked(itemModel: ItemContract.Model) {
        playlistItemDomain(itemModel)
            ?.let { itemDomain ->
                if (interactions != null) {
                    interactions?.onPlay(itemDomain)
                } else if (!(ytCastContextHolder.isConnected())) {
                    view.navigate(NavigationModel(localPlayerTarget, mapOf(PLAYLIST_ITEM to itemDomain)))
                } else {
                    itemDomain.playlistId?.let {
                        playItem(itemModel.id, itemDomain, false)
                    } ?: run {
                        val source = if (state.playlist?.type != APP) state.playlistIdentifier.source else LOCAL
                        view.showItemDescription(itemModel.id, itemDomain, source)
                    }
                }
            } // todo error
    }

    override fun onPlayStartClick(itemModel: ItemContract.Model) {
        playlistItemDomain(itemModel)
            ?.let { itemDomain ->
                if (interactions != null) {
                    interactions?.onPlayStartClick(itemDomain)
                } else if (!ytCastContextHolder.isConnected()) {
                    view.navigate(NavigationModel(localPlayerTarget, mapOf(PLAYLIST_ITEM to itemDomain)))
                } else {
                    playItem(itemModel.id, itemDomain, true)
                }
            } // todo error
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

    override fun onShowChildren(): Boolean {
        state.playlist?.id?.also {
            view.navigate(
                NavigationModel(
                    PLAYLISTS_FRAGMENT, mapOf(PLAYLIST_ID to it)
                )
            )
        }
        return true
    }

    override fun onItemShowChannel(itemModel: ItemContract.Model) {
        playlistItemDomain(itemModel)
            ?.takeUnless { ytJavaApi.launchChannel(it.media) }
            ?.also { toastWrapper.show("can't launch channel") }
            ?: toastWrapper.show("can't find video")
    }

    override fun onItemStar(itemModel: ItemContract.Model) {
        state.viewModelScope.launch {
            playlistItemDomain(itemModel)
                ?.takeIf { it.id != null }
                ?.let { it.copy(media = it.media.copy(starred = !it.media.starred)) }
                ?.also { playlistItemOrchestrator.save(it, LOCAL.deepOptions()) }
        }
    }

    override fun onItemRelated(itemModel: ItemContract.Model) {
        playlistItemDomain(itemModel)
            ?.also { item ->
                prefsWrapper.putString(
                    LAST_REMOTE_SEARCH, SearchRemoteDomain(
                        relatedToMediaPlatformId = item.media.platformId,
                        relatedToMediaTitle = item.media.title
                    ).serialise()
                )
                prefsWrapper.putEnum(LAST_SEARCH_TYPE, REMOTE)

                view.navigate(
                    PlaylistContract.makeNav(REMOTE_SEARCH_PLAYLIST, null, false, MEMORY)
                )
            }
    }

    override fun onItemShare(itemModel: ItemContract.Model) {
        playlistItemDomain(itemModel)
            ?.let { itemDomain ->
                shareWrapper.share(itemDomain.media)
            }
    }

    override fun onItemGotoPlaylist(item: ItemContract.Model) {
        playlistItemDomain(item)
            ?.playlistId
            ?.let {
                view.navigate(
                    NavigationModel(
                        PLAYLIST_FRAGMENT,
                        mapOf(PLAYLIST_ID to it, PLAY_NOW to false, SOURCE to LOCAL)
                    )
                )
            }
    }

    private fun playItem(modelId: Long, itemDomain: PlaylistItemDomain, resetPos: Boolean) {
        if (queue.playlistId == itemDomain.playlistId?.toIdentifier(LOCAL)) {
            queue.onItemSelected(itemDomain, resetPosition = resetPos)
        } else {
            view.showAlertDialog(modelMapper.mapChangePlaylistAlert({
                state.playlist?.let {
                    val toIdentifier = if (it.config.playable) {
                        state.playlistIdentifier
                    } else {
                        itemDomain.playlistId!!.toIdentifier(LOCAL)
                    }
                    prefsWrapper.putPair(CURRENT_PLAYLIST, toIdentifier.toPairType<Long>())
                    coroutines.computationScope.launch {
                        it.id?.apply { queue.switchToPlaylist(toIdentifier) }
                        queue.onItemSelected(itemDomain, forcePlay = true, resetPosition = resetPos)
                    }
                }
            }, {// info
                view.showItemDescription(modelId, itemDomain, state.playlistIdentifier.source)
            }))
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

    override fun onItemPlay(itemModel: ItemContract.Model, external: Boolean) {
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
                        view.navigate(NavigationModel(localPlayerTarget, mapOf(PLAYLIST_ITEM to it)))
                    }
                }
            }
            ?: toastWrapper.show("can't find video")
    }

    private fun playlistItemDomain(itemModel: ItemContract.Model) = state.model
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
                ?.let { playlist -> playlistMutator.moveItem(playlist, state.dragFrom!!, state.dragTo!!) }
                ?.also { state.playlist = it }
                ?.also {
                    modelMapper.map(
                        it, isPlaylistPlaying(), id = state.playlistIdentifier, playlists = state.playlistsTreeLookup,
                        pinned = isPlaylistPinned()
                    )
                        .also { view.setModel(it, false) }
                    view.highlightPlayingItem(it.currentIndex)
                }
                ?.also { playlistModified ->
                    state.viewModelScope.launch {
                        state.dragTo
                            ?.let { playlistModified.items[it] }
                            ?.let { item ->
                                item to (item.id ?: throw java.lang.IllegalStateException("Moved item has no ID"))
                                    .toIdentifier(state.playlistIdentifier.source)
                                    .flatOptions()
                            }
                            ?.let {
                                playlistItemOrchestrator.save(it.first, it.second)
                            }
                    }
                }
        } else {
            if (state.dragFrom != null || state.dragTo != null) {
                log.d("commitMove: Move failed .. ")
                refreshPlaylist()
            }
        }
        state.dragFrom = null
        state.dragTo = null
    }

    override fun setPlaylistData(plId: Long?, plItemId: Long?, playNow: Boolean, source: Source) {
        coroutines.mainScope.launch {
            plId
                ?.takeIf { it != -1L }
                ?.toIdentifier(source)
                ?.apply {
                    state.playlistIdentifier = this
                }
                ?.apply { executeRefresh() }
                ?.apply {
                    state.playlist
                        ?.indexOfItemId(plItemId)
                        ?.also { view.scrollToItem(it) }
                }
                ?.apply {
                    if (playNow) {
                        queue.playNow(state.playlistIdentifier, plItemId)
                    }
                }
                ?: run {
                    log.d("is db init; ${dbInit.isInitialized()}")
                    if (dbInit.isInitialized()) {
                        state.playlistIdentifier = prefsWrapper.getPair(LAST_PLAYLIST_VIEWED, NO_PLAYLIST.toPair()).toIdentifier()
                        log.d("id; ${state.playlistIdentifier.id}")
                        executeRefresh()
                    } else {
                        dbInit.addListener({ b: Boolean ->
                            log.d("got db init: $b")
                            if (b) {
                                state.playlistIdentifier = 3L.toIdentifier(LOCAL) // philosophy
                                refreshPlaylist()
                            }
                        })
                    }

                }
        }
    }

    override fun undoDelete() {
        state.deletedPlaylistItem?.let { itemDomain ->
            state.viewModelScope.launch {
                playlistItemOrchestrator.save(itemDomain, LOCAL.flatOptions())
                state.deletedPlaylistItem = null
                executeRefresh()
            }
        }
    }

    override fun refreshPlaylist() {
        state.viewModelScope.launch {
            try {
                state.playlist
                    ?.takeIf { playlistUpdateOrchestrator.checkToUpdate(it) }
                    ?.also { playlistUpdateOrchestrator.update(it) }
                    ?.also { view.hideRefresh() }
                    ?: executeRefresh()
            } catch (e: Exception) {
                log.e("Caught Error updating playlist", e)
                view.showError(e.message ?: "Error updating ...")
            }
        }
    }

    override suspend fun commitPlaylist(onCommit: ShareContract.Committer.OnCommit) {
        if (state.playlistIdentifier.source == MEMORY) {
            state.playlist
                ?.let { playlistMediaLookupOrchestrator.lookupMediaAndReplace(it, LOCAL) }
                ?.let {
                    it.copy(
                        items = it.items.map { it.copy(id = null) },
                        config = it.config.copy(
                            playable = true, editable = true, deletable = true, deletableItems = true, editableItems = true
                        )
                    )
                }
                ?.let { playlist ->
                    state.addPlaylistParent
                        ?.takeIf { it > 0 }
                        ?.let { parentId ->
                            playlistOrchestrator.load(parentId, LOCAL.flatOptions())
                                ?.let { playlist.copy(parentId = parentId) }
                        }
                        ?: playlist
                }
                ?.let { playlistOrchestrator.save(it, Options(LOCAL, flat = false)) }
                ?.also { state.playlistIdentifier = it.id?.toIdentifier(LOCAL) ?: throw IllegalStateException("Save failure") }
                ?.also { state.playlist = it }
                ?.also { updateView() }
                ?.also { onCommit.onCommit(PLAYLIST, listOf(it)) }
                ?.also { prefsWrapper.putLong(LAST_PLAYLIST_CREATED, it.id!!) }
        } else {
            throw IllegalStateException("Can't save non Memory playlist")
        }
    }

    override fun reloadHeader() {
        coroutines.mainScope.launch { updateHeader() }
    }

    private suspend fun executeRefresh(animate: Boolean = true, scrollToItem: Boolean = false) {
        view.showRefresh()
        try {
            log.d("executeRefresh: ${state.playlistIdentifier.id}")
            playlistOrDefaultOrchestrator
                .getPlaylistOrDefault(state.playlistIdentifier.id as Long, state.playlistIdentifier.source.flatOptions())
                .also { state.playlist = it?.first }
                ?.also { (playlist, source) ->
                    playlist.id
                        ?.also { id -> state.playlistIdentifier = id.toIdentifier(source) }
                        ?: throw IllegalStateException("Need an id")
                }
                ?.also {
                    state.playlistsTree = playlistOrchestrator
                        .loadList(OrchestratorContract.AllFilter(), LOCAL.flatOptions())
                        .buildTree()
                        .also {
                            state.playlistsTreeLookup = it.buildLookup()
                        }
                }
                ?.also {
                    if (it.second == LOCAL || it.first.type == APP) {
                        prefsWrapper.putPair(LAST_PLAYLIST_VIEWED, state.playlistIdentifier.toPairType<Long>())
                    }
                }
                .also { updateView(animate) }
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
                    it, isPlaylistPlaying(), id = state.playlistIdentifier, playlists = state.playlistsTreeLookup,
                    pinned = isPlaylistPinned()
                )
            }
            ?.also { state.model = it }
            ?.also { view.setModel(it, animate) }
            .also {
                state.focusIndex
                    ?.apply {
                        view.scrollToItem(this)
                        state.lastFocusIndex = state.focusIndex
                        state.focusIndex = null
                    }
                    ?: run {
                        state.playlist?.currentIndex
                            ?.also { view.highlightPlayingItem(it) }
                    }
            }
    }

    private suspend fun updateHeader() = withContext(coroutines.Main) {
        state.playlist
            .takeIf { coroutines.mainScopeActive }
            ?.apply {
                view.setHeaderModel(
                    modelMapper.map(
                        this, isPlaylistPlaying(), false, id = state.playlistIdentifier,
                        playlists = state.playlistsTreeLookup, pinned = isPlaylistPinned()
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
                            val changedItemDomain = plistItem ?: originalItemDomain.copy(media = media)
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
        val mappedItem = modelMapper.mapItem(
            modelId, changedItem, index,
            state.playlist?.config?.editableItems ?: false,
            state.playlist?.config?.deletableItems ?: false,
            state.playlist?.config?.editable ?: false,
            playlists = state.playlistsTreeLookup,
            currentPlaylistId = state.playlist?.id
        )
        state.model = state.model?.let {
            it.copy(items = it.items?.toMutableList()?.apply { set(index, mappedItem) })
        }?.also { it.itemsIdMap[modelId] = changedItem }

        mappedItem
            .takeIf { coroutines.mainScopeActive }
            ?.apply { view.updateItemModel(this) }
    }

    companion object {
        private val localPlayerTarget = NavigationModel.Target.LOCAL_PLAYER
    }

}