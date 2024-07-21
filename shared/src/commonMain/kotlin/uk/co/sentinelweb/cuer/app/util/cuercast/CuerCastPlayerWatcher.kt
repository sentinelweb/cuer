package uk.co.sentinelweb.cuer.app.util.cuercast

import kotlinx.coroutines.*
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.State.CastDetails
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.CastConnectionState.Connected
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.CastConnectionState.Disconnected
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.ControlTarget.CuerCast
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model.Buttons
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.domain.ext.name
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.net.remote.RemotePlayerInteractor
import uk.co.sentinelweb.cuer.remote.server.locator
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerCommandMessage.*

// using FloatingWindowMviView as a template
class CuerCastPlayerWatcher(
    private val state: State,
    private val remotePlayerInteractor: RemotePlayerInteractor,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper,
) {
    data class State(
        var lastMessage: PlayerSessionContract.PlayerStatusMessage? = null,
    )

    init {
        log.tag(this)
    }

    var remoteNode: RemoteNodeDomain? = null

    private var pollingJob: Job? = null

    var mainPlayerControls: PlayerContract.PlayerControls? = null
        get() = field
        set(value) {
            pollingJob?.cancel()
            if (field != null && value == null) {
                field?.removeListener(controlsListener)
                field?.setCastDetails(CastDetails(CuerCast, Disconnected))
            } else if (value != null) {
                value.addListener(controlsListener)
                value.setCastDetails(CastDetails(CuerCast, Connected, remoteNode?.name()))
                initPolling()
            }
            field = value
            //currentButtons?.let { value?.setButtons(it) }
        }

    fun isWatching(): Boolean = remoteNode != null

    fun isPlaying(): Boolean = state.lastMessage?.playbackState == PlayerStateDomain.PLAYING

    private fun initPolling() {
        pollingJob = coroutines.mainScope.launch {
            while (isActive && remoteNode != null) {
                val watcherLocator1 = remoteNode
                if (watcherLocator1 != null) {
                    remotePlayerInteractor
                        .playerSessionStatus(watcherLocator1.locator())
                        .also { updatePlayerState(it) }
                    delay(1000)
                } else {
                    mainPlayerControls?.setCastDetails(CastDetails(CuerCast, Disconnected))
                    cancel()
                }
            }
        }
    }

    private suspend fun updatePlayerState(result: NetResult<PlayerSessionContract.PlayerStatusMessage>) =
        withContext(coroutines.Main) {
            // todo process errors
            result.takeIf { it.isSuccessful }?.data
                ?.apply { mainPlayerControls?.setPlayerState(playbackState) }
                ?.apply { mainPlayerControls?.setPlaylistItem(item) }
                ?.apply {
                    item.media.duration?.let { mainPlayerControls?.setDuration(it / 1000f) }
                }
                ?.apply {
                    item.media.positon?.let { mainPlayerControls?.setCurrentSecond(it / 1000f) }
                }
                ?.apply { mainPlayerControls?.setButtons(Buttons(false, false, true)) }
                ?.apply { state.lastMessage = this }
        }

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
        cleanup()
    }

    private fun cleanup() {
        pollingJob?.cancel()
        pollingJob = null
        mainPlayerControls?.reset()
        mainPlayerControls = null
        remoteNode = null
    }
}
