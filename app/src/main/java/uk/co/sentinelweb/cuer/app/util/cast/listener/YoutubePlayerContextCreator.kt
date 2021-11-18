package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.google.android.gms.cast.framework.CastContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.util.android_yt_player.live.LivePlaybackContract
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class YoutubePlayerContextCreator constructor(
    private val queue: QueueMediatorContract.Consumer,
    private val log: LogWrapper,
    private val mediaSessionManager: MediaSessionContract.Manager,
    private val castWrapper: ChromeCastWrapper,
    private val timeProvider: TimeProvider,
    private val livePlayback: LivePlaybackContract.Controller,
    private val coroutines: CoroutineContextProvider,
) {

    fun createContext(
        castContext: CastContext,
        listener: YoutubeCastConnectionListener
    ) = ChromecastYouTubePlayerContext(castContext.sessionManager, listener)

    fun createConnectionListener() = YoutubeCastConnectionListener(
        YoutubeCastConnectionListener.State(),
        this,
        mediaSessionManager,
        castWrapper,
        queue,
        coroutines,
        log
    )

    fun createPlayerListener() =
        YouTubePlayerListener(
            YouTubePlayerListener.State(),
            queue,
            mediaSessionManager,
            log,
            timeProvider,
            coroutines,
            livePlayback
        )

}