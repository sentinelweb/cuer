package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import com.roche.mdas.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper

import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.ConnectionState
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.ConnectionState.*
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager

class YoutubeCastConnectionListener constructor(
    private val creator: YoutubePlayerContextCreator,
    private val mediaSessionManager: MediaSessionManager,
    private val toast: ToastWrapper,
    private val castWrapper: ChromeCastWrapper

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
        connectionState = CC_CONNECTING.also { playerUi?.setConnectionState(it) }
    }

    override fun onChromecastConnected(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        if (connectionState == CC_CONNECTED) {
            toast.show("CUER: There may be a chromecast problem - you can stop the connection using google home if you have issues")
            // taking a punt on this if it work too often then maybe try something else - e.g. a dialog
            castWrapper.killCurrentSession()
            connectionState = CC_DISCONNECTED.also { playerUi?.setConnectionState(it) }
            mediaSessionManager.destroyMediaSession()
            return
        }
        connectionState = CC_CONNECTED.also { playerUi?.setConnectionState(it) }
        youTubePlayerListener?.let {
            it.playerUi = playerUi
        } ?: setupPlayerListener(chromecastYouTubePlayerContext)
        mediaSessionManager.createMediaSession()
    }

    private fun setupPlayerListener(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        youTubePlayerListener = creator.createListener().also {
            chromecastYouTubePlayerContext?.initialize(it)
            it.playerUi = playerUi
        }
    }

    override fun onChromecastDisconnected() {
        connectionState = CC_DISCONNECTED.also { playerUi?.setConnectionState(it) }
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
        castWrapper.killCurrentSession()
    }
}