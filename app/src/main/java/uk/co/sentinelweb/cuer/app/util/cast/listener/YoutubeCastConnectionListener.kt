package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener

import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.ConnectionState

class YoutubeCastConnectionListener constructor(
    private val creator: YoutubePlayerContextCreator
) : ChromecastConnectionListener {

    private var youTubePlayerListener: YouTubePlayerListener? = null
    private var connectionState: ConnectionState? = null
    private var chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext? = null

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
        connectionState = ConnectionState.CC_CONNECTING.also { playerUi?.setConnectionState(it) }
    }

    override fun onChromecastDisconnected() {
        connectionState = ConnectionState.CC_DISCONNECTED.also { playerUi?.setConnectionState(it) }
        //chromecastYouTubePlayerContext?.release()
        youTubePlayerListener?.onDisconnected()
        youTubePlayerListener = null
    }

    override fun onChromecastConnected(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        connectionState = ConnectionState.CC_CONNECTED.also { playerUi?.setConnectionState(it) }
        this.chromecastYouTubePlayerContext = chromecastYouTubePlayerContext // same context object as in ChromecastYouTubePlayerContextWrapper
        youTubePlayerListener = creator.createListener().also {
            chromecastYouTubePlayerContext.initialize(it)
            it.playerUi = playerUi
        }
    }

    private fun restoreState() {
        connectionState?.apply { playerUi?.setConnectionState(this) }
    }

    fun isConnected():Boolean {
        return connectionState == ConnectionState.CC_CONNECTED
    }
}