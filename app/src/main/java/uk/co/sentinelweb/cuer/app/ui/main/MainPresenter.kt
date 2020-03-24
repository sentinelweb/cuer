package uk.co.sentinelweb.cuer.app.ui.main

import com.google.android.gms.cast.framework.CastContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import uk.co.sentinelweb.cuer.app.util.cast.listener.YoutubeCastConnectionListener
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract

class MainPresenter(
    private val view: MainContract.View,
    private val state: MainState,
    private val playerFragment: CastPlayerContract.PresenterExternal

) : MainContract.Presenter {

    override fun initChromecast() {
        playerFragment.initMediaRouteButton()
        view.checkPlayServices()
    }

    override fun setCastContext(sharedInstance: CastContext) {
        state.youtubePlayerContext = ChromecastYouTubePlayerContext(
            sharedInstance.sessionManager,
            YoutubeCastConnectionListener(playerFragment)
        )
    }
}