package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.google.android.gms.cast.framework.CastContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.roche.mdas.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.app.util.wrapper.LogWrapper

class YoutubePlayerContextCreator constructor(
    private val queue: QueueMediatorContract.Mediator,
    private val log: LogWrapper,
    private val mediaSessionManager: MediaSessionManager,
    private val toast: ToastWrapper,
    private val castWrapper: ChromeCastWrapper
) {

    fun createContext(
        castContext: CastContext
    ): ChromecastYouTubePlayerContextWrapper {
        val listener = YoutubeCastConnectionListener(this, mediaSessionManager, toast, castWrapper)
        return ChromecastYouTubePlayerContextWrapper(
            ChromecastYouTubePlayerContext(castContext.sessionManager, listener),
            listener
        )
    }

    fun createListener() =
        YouTubePlayerListener(YouTubePlayerListenerState(), queue, mediaSessionManager, log)
}