package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.google.android.gms.cast.framework.CastContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class YoutubePlayerContextCreator constructor(
    private val queue: QueueMediatorContract.Mediator,
    private val log: LogWrapper,
    private val mediaSessionManager: MediaSessionManager,
    private val castWrapper: ChromeCastWrapper,
    private val connectionMonitor: ConnectionMonitor
) {

    fun createContext(
        castContext: CastContext,
        listener: YoutubeCastConnectionListener
    ) = ChromecastYouTubePlayerContext(castContext.sessionManager, listener)


    fun createConnectionListener() = YoutubeCastConnectionListener(
        this,
        mediaSessionManager,
        castWrapper,
        connectionMonitor
    )

    fun createPlayerListener() =
        YouTubePlayerListener(YouTubePlayerListenerState(), queue, mediaSessionManager, log)

}