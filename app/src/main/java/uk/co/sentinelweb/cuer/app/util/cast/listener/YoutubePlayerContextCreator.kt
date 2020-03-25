package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.google.android.gms.cast.framework.CastContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract

class YoutubePlayerContextCreator {

    fun createContext(
        castContext: CastContext,
        playerUi: CastPlayerContract.PresenterExternal
    ) = ChromecastYouTubePlayerContext(
        castContext.sessionManager,
        YoutubeCastConnectionListener(playerUi, this)
    )

    fun createListener(
        playerUi: CastPlayerContract.PresenterExternal
    ) = YouTubePlayerListener(playerUi)
}