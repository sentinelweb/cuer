package uk.co.sentinelweb.cuer.app.ui.main

import android.util.Log
import com.google.android.gms.cast.framework.CastContext
import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastServiceManager
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.YoutubePlayerContextCreator
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract

class MainPresenter(
    private val view: MainContract.View,
    private val state: MainState,
    private val playerPresenter: CastPlayerContract.PlayerControls,
    private val creator: YoutubePlayerContextCreator,
    private val serviceManager: YoutubeCastServiceManager,
    private val chromeCastWrapper: ChromeCastWrapper
) : MainContract.Presenter {

    override fun initChromecast() {
        playerPresenter.initMediaRouteButton()
        playerPresenter.reset()
        if (state.youtubePlayerContext == null) {
            view.checkPlayServices()
        }
    }

    override fun onPlayServicesOk() {
        // todo remove
    }

    override fun setCastContext(castContext: CastContext) {
        // todo remove on disconnection? will need to move to a servicxe

        state.youtubePlayerContext = creator.createContext(castContext)
        state.youtubePlayerContext?.playerUi = playerPresenter
    }

    override fun onStart() {
        serviceManager.start()
        state.youtubePlayerContext?.playerUi = playerPresenter
        Log.d(TAG,"onStart()")
    }

    override fun onStop() {
        serviceManager.stop()
        state.youtubePlayerContext?.playerUi = null
        Log.d(TAG,"onStop()")
    }

    companion object {
        val TAGP = "CuerLog"
        private val TAG = "$TAGP${MainPresenter::class.java.simpleName}"

    }
}