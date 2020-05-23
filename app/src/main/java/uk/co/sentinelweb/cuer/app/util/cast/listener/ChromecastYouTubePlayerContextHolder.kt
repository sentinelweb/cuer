package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper

class ChromecastYouTubePlayerContextHolder constructor(
    private val creator: YoutubePlayerContextCreator,
    private val chromeCastWrapper: ChromeCastWrapper
) {

    private var context: ChromecastYouTubePlayerContext? = null
    private var listener: YoutubeCastConnectionListener? = null

    var playerUi: CastPlayerContract.PlayerControls? = null
        get() = field
        set(value) {
            field = value
            listener?.playerUi = field
        }

    fun create() {
        listener = creator.createConnectionListener().apply {
            this.playerUi = playerUi
            context = creator.createContext(chromeCastWrapper.getCastContext(), this)
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

}