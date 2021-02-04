package uk.co.sentinelweb.cuer.app.queue

//import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract.ConsumerListener
//import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract.ProducerListener
//import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Options.Companion.LOCAL_FLAT
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Companion.NO_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.*
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.CURRENT_PLAYLIST
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.*
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator

class QueueMediator constructor(
    private val state: QueueMediatorState,
    private val mediaOrchestrator: MediaOrchestrator,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val coroutines: CoroutineContextProvider,
    private val mediaSessionManager: MediaSessionManager,
    private val playlistMutator: PlaylistMutator,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>,
    private val log: LogWrapper
) : QueueMediatorContract.Producer, QueueMediatorContract.Consumer {

    override val currentItem: PlaylistItemDomain?
        get() = state.currentItem
    override val currentItemIndex: Int?
        get() = state.currentItem?.let { item -> state.playlist?.items?.indexOfFirst { item.id == it.id } }
    override val playlist: PlaylistDomain?
        get() = state.playlist
    override val playlistId: Identifier<*>?
        get() = if (state.playlistIdentifier != NO_PLAYLIST) state.playlistIdentifier else null

    private lateinit var _currentItemFlow: MutableStateFlow<PlaylistItemDomain?>
    override val currentItemFlow: Flow<PlaylistItemDomain?>
        get() = _currentItemFlow
    private var _currentPlaylistFlow: MutableSharedFlow<PlaylistDomain> = MutableSharedFlow()
    override val currentPlaylistFlow: Flow<PlaylistDomain>
        get() = _currentPlaylistFlow

    init {
        log.tag(this)
        state.playlistIdentifier = prefsWrapper.getPair(CURRENT_PLAYLIST, NO_PLAYLIST.toPair()).toIdentifier()
        coroutines.computationScope.launch {
            refreshQueue()
            _currentItemFlow = MutableStateFlow(state.currentItem)
        }
        listenToDb()
    }

    private fun listenToDb() {
        playlistOrchestrator.updates
            .onEach { (op, source, plist) ->
                try {
                    if (plist.id?.toIdentifier(source) == state.playlistIdentifier) {
                        when (op) {
                            FLAT -> {
                                if (!plist.matchesHeader(state.playlist)) {
                                    state.playlist?.apply { refreshQueueFrom(replaceHeader(plist), source) }
                                        ?: refreshQueueFrom(plist, source)
                                }
                            }
                            FULL -> {
                                if (plist != state.playlist) {
                                    val indexBefore = currentItemIndex
                                    refreshQueueFrom(plist, source)
                                }
                            }
                            DELETE -> {
                                refreshQueue()// should load default
                                log.e("Current playlist deleted!!")
                            }
                        }
                    }
                } catch (e: Exception) {
                    log.e("Playlist exception: $op (${plist.id}) ${plist.title}", e)
                }
            }.launchIn(coroutines.computationScope)

        playlistItemOrchestrator.updates
            .onEach { (op, source, plistItem) ->
                try {
                    when (op) {
                        FLAT,
                        FULL -> if (plistItem.playlistId?.toIdentifier(source) == state.playlistIdentifier) {
                            // todo might be issues here as if the current index is changed and isn't saved might get overwritten -  should be minimal
                            state.playlist?.apply {
                                val plist = playlistMutator.addOrReplaceItem(this, plistItem)
                                refreshQueueFrom(plist, source)
                            }
                        } else { // moved out?
                            state.playlist?.items
                                ?.find { it.id == plistItem.id && source == state.playlistIdentifier.source }
                                ?.apply { refreshQueue() }
                        }
                        DELETE -> // leave for now as item is only deleted in here for queue
                            log.d("TODO : Playlist Item deleted!!!!")
                    }
                } catch (e: Exception) {
                    log.e("Playlist item exception: $op (${plistItem.id}) ${plistItem.media.title}", e)
                }
            }.launchIn(coroutines.computationScope)
    }

    override suspend fun switchToPlaylist(identifier: Identifier<*>) {
        state.playlistIdentifier = identifier
        refreshQueue()
    }

    override fun onItemSelected(playlistItem: PlaylistItemDomain, forcePlay: Boolean, resetPosition: Boolean) {
        coroutines.computationScope.launch {
            state.playlist
                ?.takeIf { playlistItem != state.currentItem || forcePlay }
                ?.let {
                    state.playlist = playlistMutator.playItem(it, playlistItem)
                    updateCurrentItem(resetPosition)
                }
        }
    }

    override suspend fun playNow(identifier: Identifier<*>, playlistItemId: Long?) {
        playlistOrchestrator.load(identifier.id as Long, Options(identifier.source, false))
            ?.let {
                //log.d("playNow(loaded playlist= ${it.id}, requested=$playlistId, item = $playlistItemId)")
                playNow(it, playlistItemId, identifier.source)
            }
    }

    private suspend fun playNow(playlist: PlaylistDomain, playlistItemId: Long?, source: Source) {
        playlist.indexOfItemId(playlistItemId)?.let { foundIndex ->
            //log.d("playNow(load found Index= $foundIndex)")
            playlist.let {
                it.copy(currentIndex = foundIndex).apply {
                    playlistOrchestrator.save(it, Options(source, true))
                }
            }
        }?.also {
            //log.d("playNow(updated Index to = ${it.currentIndex})")
            it.id?.toIdentifier(source)?.toPair()?.let {
                prefsWrapper.putPair(CURRENT_PLAYLIST, it)
            } ?: throw IllegalArgumentException("No playlist ID")
            refreshQueueFrom(it, source)
            //log.d("playNow(state current Index is = ${state.playlist?.currentIndex})")
            playNow()
        }
    }

    override fun playNow() {
        coroutines.computationScope.launch {
            state.playlist?.apply {
                val itemToPlay = if (currentIndex == -1) {
                    items[0]
                } else if (currentIndex >= items.size) {
                    items[0]
                } else {
                    currentItem()
                }
                //log.d("playNow() item = ${itemToPlay})")
                itemToPlay?.let {
                    //log.d("playNow(item= $it)")
                    state.playlist = playlistMutator.playItem(this, it)
                    updateCurrentItem(false)
                }
            }
        }
    }

    override fun updateMediaItem(updatedMedia: MediaDomain) {
        coroutines.computationScope.launch {
            state.currentItem = state.currentItem?.run {
                val mediaUpdated = media.copy(
                    positon = updatedMedia.positon,
                    duration = updatedMedia.duration,
                    dateLastPlayed = updatedMedia.dateLastPlayed,
                    watched = true
                )
                mediaOrchestrator.save(mediaUpdated, state.playlistIdentifier.toFlat<Long>(true))
                copy(media = mediaUpdated)
            }
            state.playlist = state.playlist?.let {
                it.copy(items = it.items.toMutableList().apply {
                    set(currentItemIndex ?: throw IllegalStateException(), state.currentItem ?: throw IllegalStateException())
                })
            }
        }
    }

    override fun destroy() {
        // might not be needed if singleton
        // save queue position
        coroutines.cancel()
    }

    override fun nextItem() {
        coroutines.computationScope.launch {
            state.playlist?.let { currentPlaylist ->
                state.playlist = playlistMutator.gotoNextItem(currentPlaylist)
                if (state.playlist?.currentIndex ?: 0 < currentPlaylist.items.size) {
                    updateCurrentItem(false)
                }
            }
        }
    }

    override fun previousItem() {
        coroutines.computationScope.launch {
            state.playlist?.let { currentPlaylist ->
                state.playlist = playlistMutator.gotoPreviousItem(currentPlaylist)
                updateCurrentItem(false)
            }
        }
    }

    private suspend fun updateCurrentItem(resetPosition: Boolean) {
        state.currentItem = state.playlist
            ?.let { playlist ->
                playlistOrchestrator.updateCurrentIndex(playlist, state.playlistIdentifier.toFlat<Long>(true))
                playlist.currentIndex.let { playlist.items[it] }
            }
            ?: throw NullPointerException("playlist should not be null")
        log.d("updateCurrentItem: currentItemId=${state.currentItem?.id} currentMediaId=${state.currentItem?.media?.id} currentIndex=${state.playlist?.currentIndex} items.size=${state.playlist?.items?.size} ")
        if (resetPosition) {
            state.currentItem?.apply { updateMediaItem(media.copy(positon = 0)) }
        }
        state.currentItem?.apply {
            mediaSessionManager.setMedia(media)
        }
        //consumerListeners.forEach { it.onItemChanged() }
        _currentItemFlow.emit(state.currentItem)
        //producerListeners.forEach { it.onItemChanged() }
    }

    override fun onTrackEnded(media: MediaDomain?) {
        nextItem()
    }

    override fun refreshQueueBackground() {
        coroutines.computationScope.launch { refreshQueue() }
    }

    private suspend fun refreshQueueFrom(playlistDomain: PlaylistDomain, source: Source) {
        // if the playlist is the same then don't change the current item
        val itemBefore = currentItem
        var playlistChanged = false
        if (state.playlistIdentifier != playlistDomain.id?.toIdentifier(source)) {
            state.playlistIdentifier = playlistDomain.id?.toIdentifier(source)
                ?: throw java.lang.IllegalStateException("No playlist ID")
            state.currentItem = playlistDomain.currentItemOrStart()
            playlistChanged = true
        } else {
            state.currentItem = playlistDomain.currentItem()
        }
        state.playlist = playlistDomain
        //log.d("refreshQueueFrom: currentItemId=${state.currentItem?.id}")
        state.currentItem?.apply {
            mediaSessionManager.setMedia(media)
        }
        //consumerListeners.forEach { it.onPlaylistUpdated() }
        if (itemBefore != currentItem && this::_currentItemFlow.isInitialized) {
            _currentItemFlow.emit(currentItem)
        }
        if (playlistChanged) {
            state.playlist?.also { _currentPlaylistFlow.emit(it) }
        }
    }

    override suspend fun refreshQueue() {
        state.playlistIdentifier
            .let { playlistOrchestrator.getPlaylistOrDefault(it.id as Long, Options(it.source, flat = false)) }
            ?.also { refreshQueueFrom(it.first, it.second) }
    }

//    override fun addConsumerListener(l: ConsumerListener) {
//        consumerListeners.add(l)
//    }
//
//    override fun removeConsumerListener(l: ConsumerListener) {
//        consumerListeners.remove(l)
//    }

//    override fun addProducerListener(l: ProducerListener) {
//        producerListeners.add(l)
//    }
//
//    override fun removeProducerListener(l: ProducerListener) {
//        producerListeners.remove(l)
//    }

//    override fun deleteItem(index: Int) {
//        state.playlist?.let { plist ->
//            val deleteItem = plist.items.get(index)
//            val isCurrentItem = currentItem?.id == deleteItem.id
//            coroutines.computationScope.launch {
//                val mutated = playlistMutator.delete(plist, deleteItem)
//                playlistOrchestrator.updateCurrentIndex(mutated, Options(state.playlistIdentifier.source))
//                playlistItemOrchestrator.delete(deleteItem, Options(state.playlistIdentifier.source))
//                refreshQueueFrom(mutated to state.playlistIdentifier.source)
//                if (isCurrentItem) {
//                    if (mutated.currentIndex > -1) {
//                        currentItem?.apply { onItemSelected(this, true, true) }
//                    } else {
//                        state.currentItem = null
//                        //consumerListeners.forEach { it.onItemChanged() }
//                        _currentItemFlow.emit(state.currentItem)
//                    }
//                }
//
//            }
//        }
//    }

}
