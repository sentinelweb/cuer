package uk.co.sentinelweb.cuer.app.util.cast.listener

import androidx.annotation.VisibleForTesting
import com.roche.mdas.util.wrapper.ToastWrapper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.ConnectionState.CC_CONNECTED
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.app.util.wrapper.PhoenixWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider

// todo  .. find a proper solution to kill the cast connection
// todo this class probably doesnt detect the error as expected ...
class ConnectionMonitor constructor(
    private val toast: ToastWrapper,
    private val castWrapper: ChromeCastWrapper,
    private val coCxtProvider: CoroutineContextProvider,
    private val mediaSessionManager: MediaSessionManager,
    private val phoenixWrapper: PhoenixWrapper
) {
    private var timerJob: Job? = null

    fun setTimer(stateProvider: () -> CastPlayerContract.ConnectionState?) {
        timerJob?.cancel()
        timerJob = coCxtProvider.DefaultScope.launch {
            delay(10000)
            withContext(coCxtProvider.Main) { checkConnected(stateProvider()) }
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
    }

    // check if tried to connect but didnt get connected
    private fun checkConnected(connectionState: CastPlayerContract.ConnectionState?) {
        connectionState
            .takeIf { it != CC_CONNECTED }
            ?.also { emergencyCleanup() }
    }

    // check if already connected and got another event
    fun checkAlreadyConnected(connectionState: CastPlayerContract.ConnectionState?): Boolean =
        connectionState
            .takeIf { it == CC_CONNECTED }
            ?.let {
                emergencyCleanup()
                true
            }
            .also { cancelTimer() }
            ?: false


    @VisibleForTesting
    fun emergencyCleanup() {
        toast.show("CUER: There may be a chromecast problem - you can stop the connection using google home if you have issues")
        // taking a punt on this if it work too often then maybe try something else - e.g. a dialog
        castWrapper.killCurrentSession()
        mediaSessionManager.destroyMediaSession()
        phoenixWrapper.triggerRestart()
    }
}