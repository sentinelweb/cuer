package uk.co.sentinelweb.cuer.app.util.cuercast

import kotlinx.coroutines.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL_NETWORK
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.State.CastDetails
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.CastConnectionState.Connected
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.CastConnectionState.Disconnected
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.ControlTarget.CuerCast
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model.Buttons
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionContract
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.ext.name
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.net.remote.RemotePlayerInteractor
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.locator
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerCommandMessage.*

// using FloatingWindowMviView as a template
class CuerCastPlayerWatcher(
    private val state: State,
    private val remotePlayerInteractor: RemotePlayerInteractor,
    private val coroutines: CoroutineContextProvider,
    private val mediaSessionManager: MediaSessionContract.Manager,
    private val prefs: MultiPlatformPreferencesWrapper,
    private val remotesRepository: RemotesRepository,
    private val log: LogWrapper,
) {
    data class State(
        var lastMessage: PlayerSessionContract.PlayerStatusMessage? = null,
        var isCommunicating: Boolean = false
    )

    init {
        log.tag(this)
    }

    var remoteNode: RemoteNodeDomain? = null
        get() = field
        set(value) {
            prefs.curecastRemoteNodeName = value?.hostname
            field = value
        }

    var screen: PlayerNodeDomain.Screen? = null
        get() = field
        set(value) {
            prefs.cuerCastScreen = value?.index
            field = value
        }

    private var pollingJob: Job? = null

    val volume: Float
        get() = state.lastMessage?.volume ?: 0f

    val volumeMax: Float
        get() = state.lastMessage?.volumeMax ?: 100f

    var mainPlayerControls: PlayerContract.PlayerControls? = null
        get() = field
        set(value) {
            pollingJob?.cancel()
            if (field != null && value == null) {
                field?.removeListener(controlsListener)
                mediaSessionManager.destroyMediaSession()
                coroutines.mainScope.launch {
                    field?.setCastDetails(CastDetails(CuerCast, Disconnected))
                }
            } else if (value != null) {
                value.addListener(controlsListener)
                coroutines.mainScope.launch {
                    mediaSessionManager.checkCreateMediaSession(controlsListener)
                    value.setCastDetails(
                        CastDetails(
                            CuerCast,
                            Connected,
                            getConnectionDescription()
                        )
                    )
                }
                startPolling()
            }
            field = value
            //currentButtons?.let { value?.setButtons(it) }
        }

    fun getConnectionDescription() =
        (remoteNode?.name()
            ?.let { it + "-[" + (screen?.index ?: 0) + "]" })
            ?: "Not connected"

    fun isWatching(): Boolean = remoteNode != null

    fun isPlaying(): Boolean = state.lastMessage?.playbackState == PlayerStateDomain.PLAYING

    fun attemptRestoreConnection(playerControls: PlayerContract.PlayerControls) {
        prefs.curecastRemoteNodeName
            ?.also { name ->
                coroutines.mainScope.launch {
                    remotesRepository.getByName(name)
                        ?.also { foundNode ->
                            prefs.cuerCastScreen?.also { screenIndex ->
                                screen =
                                    remotePlayerInteractor.getPlayerConfig(foundNode.locator())
                                        .data
                                        ?.screens
                                        ?.getOrNull(screenIndex)
                                        ?.also { remoteNode = foundNode }
                                        ?.also { mainPlayerControls = playerControls }
                            }
                        }
                }
            }
    }

    private fun startPolling() {
        pollingJob = coroutines.mainScope.launch {
            try {
                while (isActive && remoteNode != null) {
                    val watcherNode = remoteNode
                    if (watcherNode != null) {
                        remotePlayerInteractor
                            .playerSessionStatus(watcherNode.locator())
                            .also { updatePlayerState(it) }
                        delay(1000)
                    } else {
                        mainPlayerControls?.setCastDetails(CastDetails(CuerCast, Disconnected))
                        cancel()
                    }
                }
            } catch (e: CancellationException) {
                log.d("polling job cancelled")
            }
        }
    }

    private suspend fun updatePlayerState(result: NetResult<PlayerSessionContract.PlayerStatusMessage>) =
        withContext(coroutines.Main) {
            result.takeIf { it.isSuccessful }
                ?.data
                ?.let { addBlankRemoteIdToItem(it) }
                ?.let { addThumbnailToMedia(it) }
                ?.apply { mainPlayerControls?.setPlayerState(playbackState) }
                ?.apply { mainPlayerControls?.setPlaylistItem(item) }
                ?.apply { item.media.duration?.let { mainPlayerControls?.setDuration(it / 1000f) } }
                ?.apply { item.media.positon?.let { mainPlayerControls?.setCurrentSecond(it / 1000f) } }
                ?.apply {// fixme enable when we have playlist
                    mainPlayerControls?.setButtons(Buttons(false, false, true))
                }
                ?.apply { mediaSessionManager.setMedia(this.item.media, null) } // fixme get playlist
                ?.apply {// fixme get liveOffset, playlist
                    mediaSessionManager.updatePlaybackState(this.item.media, this.playbackState, null, null)
                }
                ?.apply { state.lastMessage = this }
                ?.apply { state.isCommunicating = true }
                ?.apply { log.d("from host: volume:${state.lastMessage?.volume} max:${state.lastMessage?.volumeMax}") }
                ?: run {
                    state.isCommunicating = false
                    when (result) {
                        is NetResult.HttpError -> {
                            if (result.code == "503") {
                                // fixme maybe cleanup after a few request - auto disconnect at launch while player is
                                // starting
                                log.e("remote player service not available: ${remoteNode?.locator()}")
                            } else {
                                log.e("remote error (${result.code}): ${remoteNode?.locator()}")
                            }
                            mainPlayerControls?.setPlayerState(PlayerStateDomain.UNKNOWN)
                            log.d("reset media")
                            mainPlayerControls?.setPlaylistItem(null)
                            mainPlayerControls?.setButtons(Buttons(false, false, false))
                            state.lastMessage?.item?.media?.also {
                                mediaSessionManager.updatePlaybackState(it, PlayerStateDomain.UNKNOWN, null, null)
                            }
                        }
                        // remote down : java.net.ConnectException: Connection refused
                        // no network: java.net.ConnectException: Network is unreachable
                        is NetResult.Error -> {
                            log.e("cast update error: ${remoteNode?.locator()}", result.t)
                            result.t
                                //?.takeIf { it::class.qualifiedName == "java.net.ConnectException" }
                                ?.also { cleanup() }
                        }

                        is NetResult.Data -> Unit
                    }
                }
        }

    private fun addThumbnailToMedia(it: PlayerSessionContract.PlayerStatusMessage) =
        if (it.item.media.thumbNail == null) {
            it.copy(
                item = it.item.copy(
                    media = it.item.media.copy(
                        thumbNail = ImageDomain(url = "https://cuer-275020.firebaseapp.com/images/headers/pixabay-star-640-wallpaper-ga4c7c7acf_640.jpg")
                    )
                )
            )
        } else it

    private fun addBlankRemoteIdToItem(it: PlayerSessionContract.PlayerStatusMessage) =
        if (it.item.id == null) {
            it.copy(
                item = it.item.copy(
                    id = Identifier(GUID(""), LOCAL_NETWORK, remoteNode?.locator())
                )
            )
        } else it


    private val controlsListener = object : PlayerContract.PlayerControls.Listener {
        override fun play() {
            dispatchCommand(PlayPause(false))
        }

        override fun pause() {
            dispatchCommand(PlayPause(true))
        }

        override fun trackBack() {
            dispatchCommand(TrackBack)
        }

        override fun trackFwd() {
            dispatchCommand(TrackFwd)
        }

        override fun seekTo(positionMs: Long) {
            state.lastMessage?.item?.media?.apply {
                duration?.also {
                    dispatchCommand(SeekToFraction(positionMs / it.toFloat()))
                }
            }

        }

        override fun getLiveOffsetMs(): Long = 0

        override fun skipBack() {
            dispatchCommand(SkipBack)
        }

        override fun skipFwd() {
            dispatchCommand(SkipFwd)
        }
    }

    private fun dispatchCommand(command: PlayerSessionContract.PlayerCommandMessage) {
        coroutines.ioScope.launch {
            sendCommand(command)
        }
    }

    private suspend fun sendCommand(command: PlayerSessionContract.PlayerCommandMessage) {
        remoteNode?.also { remoteNode ->
            remotePlayerInteractor.playerCommand(remoteNode.locator(), command)
                .also { updatePlayerState(it) }
        }
    }

    suspend fun sendStop() = withContext(coroutines.IO) {
        sendCommand(Stop)
        withContext(coroutines.Main) { cleanup() }
    }

    suspend fun sendFocus() = withContext(coroutines.IO) {
        sendCommand(FocusWindow)
    }

    fun cleanup() {
        pollingJob?.cancel()
        pollingJob = null
        mainPlayerControls?.reset()
        mainPlayerControls = null
        remoteNode = null
        screen = null
    }

    fun sendVolume(volume: Float) = coroutines.ioScope.launch {
        sendCommand(Volume(volume))
    }
}
