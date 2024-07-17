package uk.co.sentinelweb.cuer.app.util.cuercast

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.ConnectionState.CC_CONNECTED
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.net.remote.RemotePlayerInteractor

// using FloatingWindowMviView as a template
class CuerCastPlayerWatcher(
    private val remotePlayerInteractor: RemotePlayerInteractor,
    private val coroutines: CoroutineContextProvider,
) {
    var watchLocator: Locator? = null
    private var pollingJob: Job? = null
    var mainPlayerControls: PlayerContract.PlayerControls? = null
        get() = field
        set(value) {
            if (field != null && value == null) {
                field?.removeListener(controlsListener)
                pollingJob?.cancel()
            } else if (value != null) {
                value.addListener(controlsListener)

                pollingJob = coroutines.mainScope.launch {
                    while (isActive && watchLocator != null) {
                        val watcherLocator1 = watchLocator
                        if (watcherLocator1 != null) {
                            remotePlayerInteractor.playerSessionStatus(watcherLocator1).data
                                ?.apply { mainPlayerControls?.setPlayerState(playbackState) }
                                ?.apply { mainPlayerControls?.setPlaylistItem(item) }
                                ?.apply { mainPlayerControls?.setConnectionState(CC_CONNECTED) }
                            delay(1000) // Pause for one second before the next poll
                        }
                    }
                }
            }
            field = value
            //currentButtons?.let { value?.setButtons(it) }
        }

    fun isWatching(): Boolean =
        watchLocator != null


    private val controlsListener = object : PlayerContract.PlayerControls.Listener {
        override fun play() {
            //dispatch(PlayerContract.View.Event.PlayPauseClicked(null))
        }

        override fun pause() {
            //dispatch(PlayerContract.View.Event.PlayPauseClicked(null))
        }

        override fun trackBack() {
            //dispatch(PlayerContract.View.Event.TrackBackClicked)
        }

        override fun trackFwd() {
            //dispatch(PlayerContract.View.Event.TrackFwdClicked)
        }

        override fun seekTo(positionMs: Long) {
            //dispatch(PlayerContract.View.Event.OnSeekToPosition(positionMs))
        }

        override fun getLiveOffsetMs(): Long = 0

        override fun skipBack() {
            //dispatch(PlayerContract.View.Event.SkipBackClicked)
        }

        override fun skipFwd() {
            //dispatch(PlayerContract.View.Event.SkipFwdClicked)
        }

    }
}