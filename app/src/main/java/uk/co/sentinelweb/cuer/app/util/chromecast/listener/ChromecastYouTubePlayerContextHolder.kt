package uk.co.sentinelweb.cuer.app.util.chromecast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.util.chromecast.ChromeCastWrapper

class ChromecastYouTubePlayerContextHolder(
    private val creator: YoutubePlayerContextCreator,
    private val chromeCastWrapper: ChromeCastWrapper,
) : ChromecastContract.PlayerContextHolder {

    private var context: ChromecastYouTubePlayerContext? = null
    private var listener: YoutubeCastConnectionListener? = null

    override var mainPlayerControls: PlayerContract.PlayerControls? = null
        get() = field
        set(value) {
            field = value
            listener?.playerUi = field
        }

    override fun create(playerControls: PlayerContract.PlayerControls) {
        creator.createConnectionListener().also { listener ->
            this.listener = listener
            mainPlayerControls = playerControls
            creator.createContext(chromeCastWrapper.getCastContext(), listener).also { context ->
                this@ChromecastYouTubePlayerContextHolder.context = context
                listener.setContext(context)
            }
        }
    }

    override fun isCreated() = context != null

    override fun isConnected() = isCreated() && listener?.isConnected() ?: false

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
