package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.ConnectionState
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.ConnectionState.*
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager

class YoutubeCastConnectionListener constructor(
    private val creator: YoutubePlayerContextCreator,
    private val mediaSessionManager: MediaSessionManager,
    private val castWrapper: ChromeCastWrapper//,
    //private val connectionMonitor: ConnectionMonitor
) : ChromecastConnectionListener {

    private var youTubePlayerListener: YouTubePlayerListener? = null
    private var connectionState: ConnectionState? = null

    var playerUi: CastPlayerContract.PlayerControls? = null
        get() = field
        set(value) {
            field = value
            youTubePlayerListener?.playerUi = field
            field?.let {
                restoreState()
            }
        }

    override fun onChromecastConnecting() {
        //connectionMonitor.setTimer({ connectionState })
        connectionState = CC_CONNECTING.also {
            playerUi?.setConnectionState(it)
            //connectionMonitor.connectionState = it
        }
    }

    override fun onChromecastConnected(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        //if (connectionMonitor.checkAlreadyConnected(connectionState)) return

        connectionState = CC_CONNECTED.also {
            playerUi?.setConnectionState(it)
            //connectionMonitor.connectionState = it
        }
        youTubePlayerListener?.let {
            it.playerUi = playerUi
        } ?: setupPlayerListener(chromecastYouTubePlayerContext)

        youTubePlayerListener?.apply { mediaSessionManager.createMediaSession(this) }
    }

    private fun setupPlayerListener(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        youTubePlayerListener = creator.createPlayerListener().also {
            chromecastYouTubePlayerContext.initialize(it)
            it.playerUi = playerUi
        }
    }

    override fun onChromecastDisconnected() {
        connectionState = CC_DISCONNECTED.also {
            playerUi?.setConnectionState(it)
            //connectionMonitor.connectionState = it
        }
        youTubePlayerListener?.onDisconnected()
        youTubePlayerListener = null
        mediaSessionManager.destroyMediaSession()
    }

    private fun restoreState() {
        connectionState?.apply { playerUi?.setConnectionState(this) }
    }

    fun isConnected(): Boolean {
        return connectionState == CC_CONNECTED
    }

    fun destroy() {
        //connectionMonitor.cancelTimer()
        castWrapper.killCurrentSession()
    }

}