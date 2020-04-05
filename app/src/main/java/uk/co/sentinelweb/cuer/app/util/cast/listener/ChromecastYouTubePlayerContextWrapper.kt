package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract

class ChromecastYouTubePlayerContextWrapper(
    private val context: ChromecastYouTubePlayerContext,
    private val listener: YoutubeCastConnectionListener
) {
    var playerUi: CastPlayerContract.PresenterExternal? = null
        get() = field
        set(value) {
            field = value
            listener.playerUi = field
        }
}