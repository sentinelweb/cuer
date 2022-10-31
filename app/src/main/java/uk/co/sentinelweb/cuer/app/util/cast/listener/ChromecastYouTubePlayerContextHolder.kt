package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper

class ChromecastYouTubePlayerContextHolder constructor(
    private val creator: YoutubePlayerContextCreator,
    private val chromeCastWrapper: ChromeCastWrapper
) {

    private var context: ChromecastYouTubePlayerContext? = null
    private var listener: YoutubeCastConnectionListener? = null

    var playerUi: PlayerContract.PlayerControls? = null
        get() = field
        set(value) {
            field = value
            listener?.playerUi = field
        }

    fun create() {
        listener = creator.createConnectionListener().also { listener ->
            this.playerUi = playerUi
            creator.createContext(chromeCastWrapper.getCastContext(), listener).also { context ->
                this@ChromecastYouTubePlayerContextHolder.context = context
                listener.setContext(context)
            }
        }
    }

    fun isCreated() = context != null

    fun isConnected() =
        listener?.isConnected() ?: false

    fun onDisconnected(): Unit = TODO()

    fun destroy() {
        listener?.destroy()
        listener = null
        context?.release()
        context = null
    }

    fun addConnectionListener(listener: ChromecastConnectionListener) {
        context?.addChromecastConnectionListener(listener)
    }

    fun removeConnectionListener(listener: ChromecastConnectionListener) {
        context?.removeChromecastConnectionListener(listener)
    }

}