package uk.co.sentinelweb.cuer.app.ui.main

import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastServiceManager
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.YoutubePlayerContextCreator
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract
import uk.co.sentinelweb.cuer.app.util.wrapper.LogWrapper

class MainPresenter(
    private val view: MainContract.View,
    private val state: MainState,
    private val playerControls: CastPlayerContract.PlayerControls,
    private val creator: YoutubePlayerContextCreator,
    private val ytServiceManager: YoutubeCastServiceManager,
    private val chromeCastWrapper: ChromeCastWrapper,
    private val log: LogWrapper
) : MainContract.Presenter {

    override fun initialise() {
        log.tag = "MainPresenter"
        playerControls.initMediaRouteButton()
        playerControls.reset()
        if (!ytServiceManager.isRunning() && !state.playServiceCheckDone) {
            view.checkPlayServices()
            state.playServiceCheckDone = true
        }
    }

    override fun onPlayServicesOk() {
        log.d("onPlayServicesOk()")
        state.playServicesAvailable = true
        initialiseCastContext()
    }

    private fun initialiseCastContext() {
        state.youtubePlayerContext = creator.createContext(chromeCastWrapper.getCastContext())
        state.youtubePlayerContext?.playerUi = playerControls
        log.d("initialiseCastContext()")
    }

    override fun onStart() {
        if (ytServiceManager.isRunning()) {
            log.d("onStart():svc.isRunning state_cxt:${state.youtubePlayerContext}, controls:${playerControls}")
            state.youtubePlayerContext = ytServiceManager
                .get()!!
                .popYoutubeContext()!!
            log.d("onStart():svc.isRunning from svc state_cxt:${state.youtubePlayerContext} existing controls:${state.youtubePlayerContext?.playerUi}")
            state.youtubePlayerContext?.playerUi = playerControls
        } else {
            log.d("onStart():svc.isNotRunning state_cxt:${state.youtubePlayerContext}, controls:${playerControls}")
            ytServiceManager.start() // try moving to onStop before switch start might happen in same thread
            if (state.youtubePlayerContext == null && state.playServicesAvailable) {
                initialiseCastContext()
            }
        }
    }

    override fun onStop() {
        if (state.youtubePlayerContext!!.isConnected()) {
            // move to service
            log.d("onStop(): push cxt to svc ßstate_cxt:${state.youtubePlayerContext}, controls:${playerControls}")
            state.youtubePlayerContext!!.playerUi = null
            ytServiceManager.get()!!.pushYoutubeContext(state.youtubePlayerContext!!)
        } else {
            // kill everything
            log.d("onStop(): destroy state_cxt:${state.youtubePlayerContext}, controls:${playerControls}")
            state.youtubePlayerContext!!.playerUi = null
            state.youtubePlayerContext!!.destroy()
            ytServiceManager.stop()
        }
    }

    companion object {
        val TAGP = "CuerLog"
        private val TAG = "$TAGP${MainPresenter::class.java.simpleName}"
    }
}