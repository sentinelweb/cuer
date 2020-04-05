package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.google.android.gms.cast.framework.CastContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext

class YoutubePlayerContextCreator {

    fun createContext(
        castContext: CastContext
    ):ChromecastYouTubePlayerContextWrapper {
        val listener = YoutubeCastConnectionListener(this)
        return ChromecastYouTubePlayerContextWrapper(
            ChromecastYouTubePlayerContext(castContext.sessionManager, listener),
            listener
        )
    }

    fun createListener(
    ) = YouTubePlayerListener()
}