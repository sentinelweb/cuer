package uk.co.sentinelweb.cuer.app.ui.main

import com.google.android.gms.cast.framework.CastContext
import uk.co.sentinelweb.cuer.app.util.cast.listener.YoutubePlayerContextCreator
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract

class MainPresenter(
    private val view: MainContract.View,
    private val state: MainState,
    private val playerPresenter: CastPlayerContract.PresenterExternal,
    private val creator: YoutubePlayerContextCreator
) : MainContract.Presenter {

    override fun initChromecast() {
        playerPresenter.initMediaRouteButton()
        playerPresenter.reset()
        view.checkPlayServices()
    }

    override fun setCastContext(castContext: CastContext) {
        state.youtubePlayerContext = creator.createContext(castContext, playerPresenter)
    }

}