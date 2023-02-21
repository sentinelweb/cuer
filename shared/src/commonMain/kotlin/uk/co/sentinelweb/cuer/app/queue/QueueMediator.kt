package uk.co.sentinelweb.cuer.app.queue

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import summarise
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Companion.NO_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.*
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.usecase.PlaylistMediaUpdateUsecase
import uk.co.sentinelweb.cuer.app.usecase.PlaylistOrDefaultUsecase
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.ignoreJob
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.*
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator
import uk.co.sentinelweb.cuer.domain.update.MediaPositionUpdateDomain

// fixme: note some tests are flaky - run them manually when modifying this class (or fIx them!!!)
class QueueMediator constructor(
    private val state: QueueMediatorState,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val coroutines: CoroutineContextProvider,
    private val playlistMutator: PlaylistMutator,
    private val mediaUpdate: PlaylistMediaUpdateUsecase,
    private val playlistOrDefaultUsecase: PlaylistOrDefaultUsecase,
    private val prefsWrapper: MultiPlatformPreferencesWrapper,
    private val log: LogWrapper,
    private val recentLocalPlaylists: RecentLocalPlaylists
) : QueueMediatorContract.Producer, QueueMediatorContract.Consumer {

    override val currentItem: PlaylistItemDomain?
        get() = state.currentItem
    override val currentItemIndex: Int?
        get() = state.currentItem?.let { item -> state.playlist?.items?.indexOfFirst { item.id == it.id } }
    override val playlist: PlaylistDomain?
        get() = state.playlist
    override val playlistId: Identifier<GUID>?
        get() = if (state.playlistIdentifier != NO_PLAYLIST) state.playlistIdentifier else null
    override val source: Source
        get() = state.playlistIdentifier.source

    private /*lateinit*/ var _currentItemFlow: MutableStateFlow<PlaylistItemDomain?>
    override val currentItemFlow: Flow<PlaylistItemDomain?>
        get() = _currentItemFlow.distinctUntilChanged { old, new -> old == new }.onEach { log.d("currentItemFlow: ${it?.summarise()}") }

    private var _currentPlaylistFlow: MutableSharedFlow<PlaylistDomain> = MutableSharedFlow()
    override val currentPlaylistFlow: Flow<PlaylistDomain>
        get() = _currentPlaylistFlow.distinctUntilChanged()

    init {
        log.tag(this)
        state.playlistIdentifier = prefsWrapper.currentPlayingPlaylistId
        _currentItemFlow = MutableStateFlow(state.currentItem)
        coroutines.computationScope.launch {
            playlistId?.apply { refreshQueue(this) }
        }
        listenToDb()
    }

    private fun listenToDb() {
        playlistOrchestrator.updates
            .onEach { (op, source, plist) ->
                try {
                    if (plist.id == state.playlistIdentifier) {
                        when (op) {
                            FLAT -> {
                                if (!plist.matchesHeader(state.playlist)) {
                                    state.playlist
                                        ?.apply { refreshQueueFrom(replaceHeader(plist)) }
                                        ?: refreshQueueFrom(plist)
                                }
                            }

                            FULL -> {
                                if (plist != state.playlist) {
                                    refreshQueueFrom(plist)
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
                        FULL -> if (plistItem.playlistId == state.playlistIdentifier) {
                            state.playlist
                                ?.let { playlistMutator.addOrReplaceItem(it, plistItem) }
                                ?.takeIf { it != state.playlist }
                                ?.apply { refreshQueueFrom(this) }
                        } else { // moved out?
                            state.playlist
                                ?.let { playlistMutator.remove(it, plistItem) }
                                ?.takeIf { it != state.playlist }
                                ?.apply { refreshQueueFrom(this) }
                        }

                        DELETE ->
                            state.playlist
                                ?.let { playlistMutator.remove(it, plistItem) }
                                ?.takeIf { it != state.playlist }
                                ?.apply { refreshQueueFrom(this) }
                    }
                } catch (e: Exception) {
                    log.e(
                        "Playlist item exception: $op (${plistItem.id}) ${plistItem.media.title}",
                        e
                    )
                }
            }.launchIn(coroutines.computationScope)
    }

    override suspend fun switchToPlaylist(identifier: Identifier<GUID>) {
        refreshQueue(identifier)
    }

    override fun onItemSelected(
        playlistItem: PlaylistItemDomain,
        forcePlay: Boolean,
        resetPosition: Boolean
    ) = coroutines.computationScope.launch {
        log.d("onItemSelected: changed: ${playlistItem != state.currentItem} force: $forcePlay ${playlistItem.summarise()}")
        state.playlist
            ?.takeIf { playlistItem != state.currentItem || forcePlay }
            ?.let {
                log.d("onItemSelected: update playlist")
                state.playlist = playlistMutator.playItem(it, playlistItem)
                log.d("onItemSelected: updateCurrentItem")
                updateCurrentItem(resetPosition)
                log.d("onItemSelected: finished")
            }
    }.ignoreJob()

    // todo refactor / consolidate the playNow's
    override suspend fun playNow(identifier: Identifier<GUID>, playlistItemId: Identifier<GUID>?) {
        playlistOrchestrator.loadById(identifier.id, Options(identifier.source, false))
            ?.let {
                playNow(it, playlistItemId, identifier.source)
            }
    }

    private suspend fun playNow(playlist: PlaylistDomain, playlistItemId: Identifier<GUID>?, source: Source) {
        (playlist.indexOfItemId(playlistItemId)
            ?.let { foundIndex ->
                playlist.copy(currentIndex = foundIndex).apply {
                    playlistOrchestrator.save(this, Options(source, true))
                }
            }
            ?: playlist)
            .also {
                refreshQueueFrom(it)
                playNow()
            }
    }

    private fun playNow() = coroutines.computationScope.launch {
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
    }.ignoreJob()

    private suspend fun updateCurrentItem(resetPosition: Boolean) {
        log.d("updateCurrentItem: state.playlist=null: ${state.playlist == null}")
        state.currentItem = state.playlist
            ?.let { playlist ->
                playlistOrDefaultUsecase.updateCurrentIndex(
                    playlist,
                    state.playlistIdentifier.flatOptions(true)
                )
                playlist.currentIndex.let { playlist.items[it] }
            }
            ?: throw NullPointerException("playlist should not be null")
        log.d("updateCurrentItem: playFromStart: ${state.currentItem?.media?.playFromStart}")
        state.currentItem?.let {
            if (resetPosition || it.media.playFromStart || state.playlist?.playItemsFromStart ?: false) {
                log.d("updateCurrentItem:resetMediaPosition: ${it.summarise()}")
                updateCurrentItemFromMedia(it.media.copy(positon = 0))
            }
        }
        log.d("emit item: ${state.currentItem?.summarise()}")
        _currentItemFlow.emit(state.currentItem)
        log.d("emitted item: ${state.currentItem?.summarise()}")
    }

    override fun updateCurrentMediaItem(updatedMedia: MediaDomain) {
        val currentItemIsInPlaylist = (currentItemIndex ?: -1) > -1
        if (currentItemIsInPlaylist) {
            coroutines.computationScope.launch {
                updateCurrentItemFromMedia(updatedMedia)
            }
        }
    }

    private suspend fun updateCurrentItemFromMedia(updatedMedia: MediaDomain) {
        log.d("updateCurrentItemFromMedia: item.null=${state.currentItem != null} ${updatedMedia.summarise()}")
        state.currentItem = state.currentItem
            ?.run {
                media.let {
                    MediaPositionUpdateDomain(
                        id = it.id!!,
                        positon = updatedMedia.positon,
                        duration = updatedMedia.duration,
                        dateLastPlayed = updatedMedia.dateLastPlayed,
                        watched = true
                    )
                }.let {
                    log.d("updateCurrentItemFromMedia: media update: pl.null=${playlist == null} ${it}")
                    //log.d("updateCurrentMediaItem b4 sv: position=${state.currentItem?.media?.positon} target=${it.positon}")
                    val x = mediaUpdate.updateMedia(playlist!!, it, state.playlistIdentifier.flatOptions(true))
                        ?.let { copy(media = it) }
                    log.d("mediaUpdate complete ;$x")
                    x
                }
            }
        log.d("updateCurrentItemFromMedia: state.currentItem:${state.currentItem?.media?.positon}")
        state.playlist = state.playlist
            ?.let {
                it.copy(items = it.items.toMutableList().apply {
                    set(
                        currentItemIndex ?: throw IllegalStateException(),
                        state.currentItem ?: throw IllegalStateException()
                    )
                })
            }
        log.d("updateCurrentItemFromMedia: finished")
    }

    override fun destroy() {
        // might not be needed if singleton
        // save queue position
        coroutines.cancel()
    }

    override fun nextItem() = coroutines.computationScope.launch {
        state.playlist?.let { currentPlaylist ->
            state.playlist = playlistMutator.gotoNextItem(currentPlaylist)
            if ((state.playlist?.currentIndex ?: 0) < currentPlaylist.items.size) {
                updateCurrentItem(false)
            }
        }
    }.ignoreJob()

    override fun previousItem() = coroutines.computationScope.launch {
        state.playlist?.let { currentPlaylist ->
            state.playlist = playlistMutator.gotoPreviousItem(currentPlaylist)
            updateCurrentItem(false)
        }
    }.ignoreJob()

    override fun onTrackEnded() {
        nextItem()
    }

    private suspend fun refreshQueueFrom(playlistDomain: PlaylistDomain) {
        // if the playlist is the same then don't change the current item
        val playlistIdentifier = playlistDomain.id
        if (state.playlistIdentifier != playlistIdentifier) {
            state.playlistIdentifier = playlistIdentifier
                ?: throw IllegalStateException("No playlist ID")
            state.currentItem = playlistDomain.currentItemOrStart()
            playlistIdentifier.also { prefsWrapper.currentPlayingPlaylistId = it }
        } else {
            state.currentItem = playlistDomain.currentItem()
        }
        if (state.playlist?.id != playlistDomain.id) {
            recentLocalPlaylists.addRecent(playlistDomain)
        }
        state.playlist = playlistDomain
        //log.d("playlist: ${state.playlist?.scanOrder()}")
        //if (this::_currentItemFlow.isInitialized) {
        _currentItemFlow.emit(currentItem)
        //}
        state.playlist?.also { _currentPlaylistFlow.emit(it) }
    }

    private suspend fun refreshQueue(identifier: Identifier<GUID>) {
        identifier
            .let { playlistOrDefaultUsecase.getPlaylistOrDefault(it) }
            ?.also { refreshQueueFrom(it) }
    }
}
