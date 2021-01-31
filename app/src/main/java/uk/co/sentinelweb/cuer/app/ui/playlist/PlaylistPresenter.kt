package uk.co.sentinelweb.cuer.app.ui.playlist

import androidx.lifecycle.viewModelScope
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Options.Companion.LOCAL_DEEP
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Options.Companion.LOCAL_FLAT
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.dialog.playlist.PlaylistSelectDialogModelCreator
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.CURRENT_PLAYLIST_ID
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.LAST_PLAYLIST_VIEWED_ID
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.*
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator

class PlaylistPresenter(
    private val view: PlaylistContract.View,
    private val state: PlaylistContract.State,
    private val mediaOrchestrator: MediaOrchestrator,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val modelMapper: PlaylistModelMapper,
    private val queue: QueueMediatorContract.Producer,
    private val toastWrapper: ToastWrapper,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val chromeCastWrapper: ChromeCastWrapper,
    private val ytJavaApi: YoutubeJavaApiWrapper,
    private val shareWrapper: ShareWrapper,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>,
    private val playlistMutator: PlaylistMutator,
    private val log: LogWrapper,
    private val playlistDialogModelCreator: PlaylistSelectDialogModelCreator,
    private val timeProvider: TimeProvider,
    private val coroutines: CoroutineContextProvider
) : PlaylistContract.Presenter/*, QueueMediatorContract.ProducerListener*/ {

    init {
        log.tag(this)
    }

    private fun isPlaylistPlaying() = isQueuedPlaylist && ytContextHolder.isConnected()

    private val isQueuedPlaylist: Boolean
        get() = state.playlist?.let { queue.playlistId == it.id } ?: false

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
        ytContextHolder.addConnectionListener(castConnectionListener)
        listenToRepository()
    }

    private fun listenToRepository() {
        coroutines.mainScope.launch {// this may mis an update if it occur dureing a rotation
            playlistOrchestrator.updates.collect { (op, plist) ->
                when (op) {
                    FLAT -> state.playlist = state.playlist?.replaceHeader(plist)
                        ?.apply { updateHeader() }
                    FULL -> {
                        state.playlist = plist
                        updateView()
                    }
                    DELETE -> toastWrapper.show("TODO : Playlist deleted!!!!")// todo exit screen?
                }
            }
        }
        coroutines.computationScope.launch {
            playlistItemOrchestrator.updates.collect { (op, plistItem) ->
                state.playlist?.items?.find { it.id == plistItem.id }
                    ?.let { foundItem ->
                        when (op) {
                            FLAT,
                            FULL -> if (plistItem.playlistId == state.playlistId) {
                                state.playlist = state.playlist?.replaceItem(plistItem)
                            } else {
                                state.playlist = state.playlist?.removeItem(plistItem)
                            }
                            DELETE ->
                                state.playlist = state.playlist?.removeItem(plistItem)
                        }
                    }?.apply { this@PlaylistPresenter.updateView(true) }
                    .let { Unit }
            }
        }
    }

    override fun onPause() {
        ytContextHolder.removeConnectionListener(castConnectionListener)
        coroutines.cancel()
    }

    private fun queueExecIfElse(
        block: QueueMediatorContract.Producer.() -> Unit,
        elseBlock: (QueueMediatorContract.Producer.() -> Unit)? = null
    ) {
        if (isQueuedPlaylist) block(queue)
        else elseBlock?.let { it(queue) }
    }

    private fun queueExecIf(block: QueueMediatorContract.Producer.() -> Unit) {
        if (isQueuedPlaylist) block(queue)
    }


    override fun initialise() {
        //queue.addProducerListener(this)
        state.playlistId = prefsWrapper.getLong(CURRENT_PLAYLIST_ID)
        //log.d("initialise state.playlistId=${state.playlistId}")
    }

    override fun refreshList() {
        refreshPlaylist()
    }

    override fun setFocusMedia(mediaDomain: MediaDomain) {
        //state.addedMedia = mediaDomain
    }

    override fun destroy() {
        //queue.removeProducerListener(this)
    }

    override fun onItemSwipeRight(item: ItemContract.Model) {// move
        state.viewModelScope.launch {
            state.selectedPlaylistItem = state.playlist?.itemWitId(item.id)
            playlistDialogModelCreator.loadPlaylists { allPlaylists ->
                view.showPlaylistSelector(
                    playlistDialogModelCreator.mapPlaylistSelectionForDialog(
                        allPlaylists, setOf(state.playlist!!), false,
                        itemClick = { which: Int, _ ->
                            if (which < allPlaylists.size) {
                                allPlaylists[which].id?.let { moveItemToPlaylist(it) }
                            } else {
                                view.showPlaylistCreateDialog()
                            }
                        },
                        dismiss = { view.resetItemsState() }
                    )
                )
            }
        }
    }

    override fun onPlaylistSelected(playlist: PlaylistDomain) {
        playlist.id?.let { moveItemToPlaylist(it) }
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

    private fun commitHeaderChange(plist: PlaylistDomain) {
        state.viewModelScope.launch {
            playlistOrchestrator.save(plist, LOCAL_FLAT)
            //queueExecIf { refreshHeaderData() } // todo remove this and add flow collector
        }
    }

    override fun onPlayPlaylist(): Boolean {
        if (isPlaylistPlaying()) {
            chromeCastWrapper.killCurrentSession()
        } else {
            state.playlist?.let {
                prefsWrapper.putLong(CURRENT_PLAYLIST_ID, it.id!!)
                //queue.refreshQueueFrom(it)
                coroutines.computationScope.launch {
                    it.id?.apply { queue.switchToPlaylist(this) }
                    it.currentItemOrStart()?.let { queue.onItemSelected(it, forcePlay = true, resetPosition = false) }
                        ?: toastWrapper.show("No items to play")
                }
            }
            if (!ytContextHolder.isConnected()) {
                view.showCastRouteSelectorDialog()
            }
        }

        return true
    }

    override fun onStarPlaylist(): Boolean {
        state.playlist?.let { commitHeaderChange(it.copy(starred = !it.starred)) }

        return true
    }

    override fun onFilterNewItems(): Boolean {
        log.d("onFilterNewItems")
        return true
    }

    override fun onEdit(): Boolean {
        state.playlist
            ?.run { id }
            ?.apply { view.gotoEdit(this) }
        return true
    }

    override fun onFilterPlaylistItems(): Boolean {
        log.d("onFilterPlaylistItems")
        return true
    }

    private fun moveItemToPlaylist(it: Long) {
        state.selectedPlaylistItem?.let { moveItem ->
            state.viewModelScope.launch {
                moveItem.copy(playlistId = it, order = timeProvider.currentTimeMillis())
                    .apply { playlistItemOrchestrator.save(this, LOCAL_FLAT) }
                // todo update playlist pointer
                executeRefresh()
                // queueExecIf { refreshQueueFrom(state.playlist!!) }
            }
        }
    }

    override fun onItemSwipeLeft(item: ItemContract.Model) {
        state.viewModelScope.launch {
            delay(400)

            state.playlist?.let { plist ->
                plist.items.let { items ->
                    items.find { it.id == item.id }?.let { deleteItem ->
                        val indexOf = items.indexOf(deleteItem)
                        queueExecIfElse({
                            state.deletedPlaylistItem = deleteItem
                            deleteItem(indexOf)
                            view.showDeleteUndo("Deleted: ${deleteItem.media.title}")
                        }, {
                            state.viewModelScope.launch {
                                state.deletedPlaylistItem = deleteItem
                                val mutated = playlistMutator.delete(plist, deleteItem)
                                // todo handle errors
                                playlistItemOrchestrator.delete(deleteItem, LOCAL_FLAT)
                                playlistOrchestrator.updateCurrentIndex(mutated, LOCAL_FLAT)
                                view.showDeleteUndo("Deleted: ${deleteItem.media.title}")
                                executeRefresh(false)
                            }
                        })
                    }
                }
            }
        }
    }

    override fun onItemViewClick(item: ItemContract.Model) {
        state.playlist?.itemWitId(item.id)
            ?.apply { view.showItemDescription(this) }
    }

    override fun onItemClicked(item: ItemContract.Model) {
        state.playlist?.itemWitId(item.id)?.let { itemDomain ->
            if (!(ytContextHolder.isConnected())) {
                view.showItemDescription(itemDomain)
            } else {
                playItem(itemDomain, false)
            }
        } // todo error
    }

    override fun onPlayStartClick(item: ItemContract.Model) {
        state.playlist?.itemWitId(item.id)?.let { itemDomain ->
            if (!(ytContextHolder.isConnected())) {
                //view.showItemDescription(itemDomain)
            } else {
                playItem(itemDomain, true)
            }
        } // todo error
    }

    private fun playItem(itemDomain: PlaylistItemDomain, resetPos: Boolean) {
        if (isQueuedPlaylist) {
            queue.onItemSelected(itemDomain, resetPosition = resetPos)
        } else { // todo only confirm if video is playing
            view.showAlertDialog(modelMapper.mapChangePlaylistAlert({
                state.playlist?.let {
                    prefsWrapper.putLong(CURRENT_PLAYLIST_ID, it.id!!)
                    //queue.refreshQueueFrom(it)
                    coroutines.computationScope.launch {
                        it.id?.apply { queue.switchToPlaylist(this) }
                        queue.onItemSelected(itemDomain, forcePlay = true, resetPosition = resetPos)
                    }
                }
            }, {// info
                view.showItemDescription(itemDomain)
            }))
        }
    }

    override fun scroll(direction: PlaylistContract.ScrollDirection) {
        view.scrollTo(direction)
    }

    override fun onItemPlay(item: ItemContract.Model, external: Boolean) {
        state.playlist?.itemWitId(item.id)?.let { itemDomain ->
            if (external) {
                if (!ytJavaApi.launchVideo(itemDomain.media)) {
                    toastWrapper.show("can't launch video")
                }
            } else {
                view.playLocal(itemDomain.media)
            }
        } ?: toastWrapper.show("can't find video")
    }

    override fun onItemShowChannel(item: ItemContract.Model) {
        state.playlist?.itemWitId(item.id)?.let { itemDomain ->
            if (!ytJavaApi.launchChannel(itemDomain.media)) {
                toastWrapper.show("can't launch channel")
            }
        } ?: toastWrapper.show("can't find video")
    }

    override fun onItemStar(item: ItemContract.Model) {
        toastWrapper.show("todo: star ${item.id}")
    }

    override fun onItemShare(item: ItemContract.Model) {
        state.playlist?.itemWitId(item.id)?.let { itemDomain ->
            shareWrapper.share(itemDomain.media)
        }
    }

    override fun moveItem(fromPosition: Int, toPosition: Int) {
        if (state.dragFrom == null) {
            state.dragFrom = fromPosition
        }
        state.dragTo = toPosition
    }

    override fun commitMove() {
        if (state.dragFrom != null && state.dragTo != null) {
            state.playlist = state.playlist?.let { playlist ->
                playlistMutator.moveItem(playlist, state.dragFrom!!, state.dragTo!!)
            }?.also { plist ->
                plist.let { modelMapper.map(it, isPlaylistPlaying()) }
                    .also { view.setModel(it, false) }
            }?.also { plist ->
                state.viewModelScope.launch {
                    playlistOrchestrator.save(plist, LOCAL_DEEP)
                        ?: toastWrapper.show("Couldn't save playlist")
                    queueExecIf {
                        //refreshQueueFrom(plist) // refresh from flow
                        view.highlightPlayingItem(currentItemIndex)
                    }
                }
            }
        } else {
            //toastWrapper.show("Move failed ..")
            log.d("commitMove: Move failed .. ")
            refreshPlaylist()
        }
        state.dragFrom = null
        state.dragTo = null
    }

    override fun setPlaylistData(plId: Long?, plItemId: Long?, playNow: Boolean) {
        state.initialLoadJob?.apply { cancel() } // if called from resume and attach
        state.initialLoadJob = state.viewModelScope.launch {
            plId
                ?.takeIf { it != -1L }
                ?.apply {
                    prefsWrapper.putLong(LAST_PLAYLIST_VIEWED_ID, plId)
                    state.playlistId = plId
                    executeRefresh(scrollToItem = true)
                    //log.d("setPlaylistData(pl=$plId , state.pl=${state.playlist?.id} , pli=$plItemId, play=$playNow)")
                    if (playNow) {
                        state.playlist?.apply {
                            //log.d("setPlaylistData.play(pl=$plId , state.pl=${state.playlist?.id} , pli=$plItemId, play=$playNow)")
                            queue.playNow(this, plItemId)
                            state.playlist = queue.playlist?.copy()
                        }
                    } else {
                        state.playlist?.apply {
                            indexOfItemId(plItemId)?.also { foundIndex ->
                                view.scrollToItem(foundIndex)
                            }
                        }
                    }
                    queueExecIf {
                        view.highlightPlayingItem(queue.currentItemIndex)
                        currentItemIndex?.apply { view.scrollToItem(this) }
                    }
                } ?: run {
                state.playlistId = prefsWrapper.getLong(LAST_PLAYLIST_VIEWED_ID)
                executeRefresh()
            }
            queueExecIf {
                coroutines.mainScope.launch {
                    queue.currentItemFlow.collect { view.highlightPlayingItem(queue.currentItemIndex) }
                }
            }
        }
    }

    override fun undoDelete() {
        state.deletedPlaylistItem?.let { itemDomain ->
            state.viewModelScope.launch {
                playlistItemOrchestrator.save(itemDomain, LOCAL_FLAT.copy(emit = false))
                state.focusIndex = state.lastFocusIndex
                executeRefresh()
                (state.playlist?.items?.indexOfFirst { it.id == itemDomain.id } ?: -2).let { restoredIndex ->
                    if (restoredIndex >= 0) {
                        state.playlist?.currentIndex?.also { currentIndex ->
                            if (restoredIndex <= currentIndex) {
                                state.playlist = state.playlist?.copy(currentIndex = currentIndex + 1)
                                state.playlist?.let { playlistOrchestrator.updateCurrentIndex(it, LOCAL_FLAT.copy(emit = false)) }
                                view.scrollToItem(restoredIndex)
                                queueExecIf {
                                    refreshQueueBackground()
                                }
                                //view.highlightPlayingItem(currentIndex + 1)
                            }
                        }
                    }
                }
                state.deletedPlaylistItem = null
                //queueExecIf { refreshQueueFrom(state.playlist ?: throw java.lang.IllegalStateException("playlist is null")) }
            }
        }
    }

//    override fun onPlaylistUpdated(list: PlaylistDomain) {
//        state.playlist = list
//        state.viewModelScope.launch {
//            updateView()
//        }
//    }

//    override fun onItemChanged() {
//        queueExecIf {
//            currentItemIndex?.apply {
//                state.playlist = state.playlist?.copy(currentIndex = this)
//            } ?: throw IllegalStateException("Current item is null")
//            view.highlightPlayingItem(currentItemIndex)
//            currentItemIndex?.apply { view.scrollToItem(this) }
//        }
//    }

    private fun refreshPlaylist() {
        state.viewModelScope.launch { executeRefresh() }
    }

    private suspend fun executeRefresh(animate: Boolean = true, scrollToItem: Boolean = false) {
        //log.d("executeRefresh state.playlistId=${state.playlistId}")
        try {
            playlistOrchestrator.getPlaylistOrDefault(state.playlistId, LOCAL_DEEP)
                .also { state.playlist = it }
                ?.also { state.playlistId = it.id }
                .also { updateView(animate) }
        } catch (e: Throwable) {
            log.e("Error loading playlist", e)
        }
    }

    private suspend fun updateView(animate: Boolean = true, scrollToItem: Boolean = false) = withContext(coroutines.Main) {
        state.playlist
            .also { view.setSubTitle(state.playlist?.title ?: "No playlist" + (if (isQueuedPlaylist) " - playing" else "")) }
            ?.let { modelMapper.map(it, isPlaylistPlaying()) }
            ?.also { view.setModel(it, animate) }
            .also {
                state.focusIndex?.apply {
                    view.scrollToItem(this)
                    state.lastFocusIndex = state.focusIndex
                    state.focusIndex = null
                } ?: run {
                    state.playlist?.currentIndex?.also {
                        if (scrollToItem) {
                            view.scrollToItem(it)
                        }
                        view.highlightPlayingItem(it)
                    }
                }
            }
    }

    private suspend fun updateHeader() = withContext(coroutines.Main) {
        state.playlist?.apply { view.setHeaderModel(modelMapper.map(this, isPlaylistPlaying(), false)) }
    }

}