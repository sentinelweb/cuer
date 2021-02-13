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
    override val source: Source
        get() = state.playlistIdentifier.source

    private lateinit var _currentItemFlow: MutableStateFlow<PlaylistItemDomain?>
    override val currentItemFlow: Flow<PlaylistItemDomain?>
        get() = _currentItemFlow.distinctUntilChanged { old, new -> old == new }
    private var _currentPlaylistFlow: MutableSharedFlow<PlaylistDomain> = MutableSharedFlow()
    override val currentPlaylistFlow: Flow<PlaylistDomain>
        get() = _currentPlaylistFlow.distinctUntilChanged()

    init {
        log.tag(this)
        state.playlistIdentifier = prefsWrapper.getPair(CURRENT_PLAYLIST, NO_PLAYLIST.toPair()).toIdentifier()
        coroutines.computationScope.launch {
            refreshQueue(state.playlistIdentifier)
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
                                    state.playlist
                                        ?.apply { refreshQueueFrom(replaceHeader(plist), source) }
                                        ?: refreshQueueFrom(plist, source)
                                }
                            }
                            FULL -> {
                                if (plist != state.playlist) {
                                    refreshQueueFrom(plist, source)
                                }
                            }
                            DELETE -> {
                                _currentItemFlow.emit(null)
                                refreshQueue(state.playlistIdentifier)// should load default
                            }
                        }
                    }
                } catch (e: Exception) {
                    log.e("Playlist exception: $op (${plist.id}) ${plist.title}", e)
                }
            }.launchIn(coroutines.computationScope)

        playlistItemOrchestrator.updates
            .onEach { (op, source, plistItem) ->
                log.d("item changed: $op, $source, ${plistItem.id} ${plistItem.media.title}")
                try {
                    when (op) {
                        FLAT,
                        FULL -> if (plistItem.playlistId?.toIdentifier(source) == state.playlistIdentifier) {
                            state.playlist
                                ?.let { playlistMutator.addOrReplaceItem(it, plistItem) }
                                ?.takeIf { it != state.playlist }
                                ?.apply { refreshQueueFrom(this, source) }
                        } else { // moved out?
                            state.playlist
                                ?.let { playlistMutator.remove(it, plistItem) }
                                ?.takeIf { it != state.playlist }
                                ?.apply { refreshQueueFrom(this, source) }
                        }
                        DELETE ->
                            state.playlist
                                ?.let { playlistMutator.remove(it, plistItem) }
                                ?.takeIf { it != state.playlist }
                                ?.apply { refreshQueueFrom(this, source) }
                    }
                } catch (e: Exception) {
                    log.e("Playlist item exception: $op (${plistItem.id}) ${plistItem.media.title}", e)
                }
            }.launchIn(coroutines.computationScope)
    }

    override suspend fun switchToPlaylist(identifier: Identifier<*>) {
        refreshQueue(identifier)
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

    // todo refactor / consolidate the playNow's
    override suspend fun playNow(identifier: Identifier<*>, playlistItemId: Long?) {
        playlistOrchestrator.load(identifier.id as Long, Options(identifier.source, false))
            ?.let {
                playNow(it, playlistItemId, identifier.source)
            }
    }

    private suspend fun playNow(playlist: PlaylistDomain, playlistItemId: Long?, source: Source) {
        playlist.indexOfItemId(playlistItemId)?.let { foundIndex ->
            playlist.let {
                it.copy(currentIndex = foundIndex).apply {
                    playlistOrchestrator.save(it, Options(source, true))
                }
            }
        }?.also {
            refreshQueueFrom(it, source)
            playNow()
        }
    }

    private fun playNow() {
        coroutines.computationScope.launch {
            state.playlist?.apply {
                val itemToPlay = if (currentIndex == -1) {
                    items[0]
                } else if (currentIndex >= items.size) {
                    items[0]
                } else {
                    currentItem()
                }
                itemToPlay?.let {
                    state.playlist = playlistMutator.playItem(this, it)
                    updateCurrentItem(false)
                }
            }
        }
    }

    override fun updateCurrentMediaItem(updatedMedia: MediaDomain) {
        coroutines.computationScope.launch {
            state.currentItem = state.currentItem?.run {
                val mediaUpdated = media.copy(
                    positon = updatedMedia.positon,
                    duration = updatedMedia.duration,
                    dateLastPlayed = updatedMedia.dateLastPlayed,
                    watched = true
                )
                mediaOrchestrator.save(mediaUpdated, state.playlistIdentifier.toFlatOptions(true))
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
                playlistOrchestrator.updateCurrentIndex(playlist, state.playlistIdentifier.toFlatOptions(true))
                playlist.currentIndex.let { playlist.items[it] }
            }
            ?: throw NullPointerException("playlist should not be null")
        log.d("updateCurrentItem: currentItemId=${state.currentItem?.id} currentMediaId=${state.currentItem?.media?.id} currentIndex=${state.playlist?.currentIndex} items.size=${state.playlist?.items?.size} ")
        if (resetPosition) {
            state.currentItem?.apply { updateCurrentMediaItem(media.copy(positon = 0)) }
        }
        state.currentItem?.apply {
            mediaSessionManager.setMedia(media)
        }
        _currentItemFlow.emit(state.currentItem)
    }

    override fun onTrackEnded(media: MediaDomain?) {
        nextItem()
    }

    private suspend fun refreshQueueFrom(playlistDomain: PlaylistDomain, source: Source) {
        // if the playlist is the same then don't change the current item
        val playlistIdentifier = playlistDomain.id?.toIdentifier(source)
        if (state.playlistIdentifier != playlistIdentifier) {
            state.playlistIdentifier = playlistIdentifier
                ?: throw java.lang.IllegalStateException("No playlist ID")
            state.currentItem = playlistDomain.currentItemOrStart()
            playlistIdentifier.toPair().apply { prefsWrapper.putPair(CURRENT_PLAYLIST, this) }
        } else {
            state.currentItem = playlistDomain.currentItem()
        }
        state.playlist = playlistDomain
        state.currentItem?.apply {
            mediaSessionManager.setMedia(media)
        }
        log.d("playlist: ${state.playlist?.scanOrder()}")
        if (this::_currentItemFlow.isInitialized) {
            _currentItemFlow.emit(currentItem)
        }
        state.playlist?.also { _currentPlaylistFlow.emit(it) }
    }

    private suspend fun refreshQueue(identifier: Identifier<*>) {
        identifier
            .let { playlistOrchestrator.getPlaylistOrDefault(it.id as Long, Options(it.source, flat = false)) }
            ?.also { refreshQueueFrom(it.first, it.second) }
    }

}
