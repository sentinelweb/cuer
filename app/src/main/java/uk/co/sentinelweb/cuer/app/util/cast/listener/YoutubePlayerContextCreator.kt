package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.google.android.gms.cast.framework.CastContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract

class YoutubePlayerContextCreator constructor(
    private val queue: QueueMediatorContract.Mediator
) {

    fun createContext(
        castContext: CastContext
    ): ChromecastYouTubePlayerContextWrapper {
        val listener = YoutubeCastConnectionListener(this)
        return ChromecastYouTubePlayerContextWrapper(
            ChromecastYouTubePlayerContext(castContext.sessionManager, listener),
            listener
        )
    }

    fun createListener() = YouTubePlayerListener(YouTubePlayerListenerState(), queue)
}