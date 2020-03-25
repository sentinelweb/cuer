package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener

import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract

class YoutubeCastConnectionListener constructor(
    private val player: CastPlayerContract.PresenterExternal,
    private val creator: YoutubePlayerContextCreator
) : ChromecastConnectionListener {

    private var youTubePlayerListener:YouTubePlayerListener? = null

    override fun onChromecastConnecting() {
        player.setConnectionState(CastPlayerContract.ConnectionState.CC_CONNECTING)
    }

    override fun onChromecastDisconnected() {
        player.setConnectionState(CastPlayerContract.ConnectionState.CC_DISCONNECTED)
        youTubePlayerListener?.cleanup()
        youTubePlayerListener = null
    }


    override fun onChromecastConnected(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        player.setConnectionState(CastPlayerContract.ConnectionState.CC_CONNECTED)
        youTubePlayerListener = creator.createListener(player).apply {
            chromecastYouTubePlayerContext.initialize(this)
        }
    }
}