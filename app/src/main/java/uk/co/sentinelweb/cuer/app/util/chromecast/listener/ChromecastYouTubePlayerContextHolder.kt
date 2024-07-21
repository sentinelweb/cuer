package uk.co.sentinelweb.cuer.app.util.chromecast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.util.chromecast.ChromeCastWrapper

class ChromecastYouTubePlayerContextHolder(
    private val creator: YoutubePlayerContextCreator,
    private val chromeCastWrapper: ChromeCastWrapper
) : ChromecastContract.PlayerContextHolder {

    private var context: ChromecastYouTubePlayerContext? = null
    private var listener: YoutubeCastConnectionListener? = null

    override var playerUi: PlayerContract.PlayerControls? = null
        get() = field
        set(value) {
            field = value
            listener?.playerUi = field
        }

    override fun create() {
        listener = creator.createConnectionListener().also { listener ->
            this.playerUi = playerUi
            creator.createContext(chromeCastWrapper.getCastContext(), listener).also { context ->
                this@ChromecastYouTubePlayerContextHolder.context = context
                listener.setContext(context)
            }
        }
    }

    override fun isCreated() = context != null

    override fun isConnected() =
        listener?.isConnected() ?: false

    override fun onDisconnected(): Unit = TODO()

    override fun destroy() {
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