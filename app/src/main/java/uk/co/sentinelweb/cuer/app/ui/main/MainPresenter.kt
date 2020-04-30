package uk.co.sentinelweb.cuer.app.ui.main

import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastServiceManager
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract
import uk.co.sentinelweb.cuer.app.util.wrapper.LogWrapper

class MainPresenter(
    private val view: MainContract.View,
    private val state: MainState,
    private val playerControls: CastPlayerContract.PlayerControls,
    private val ytServiceManager: YoutubeCastServiceManager,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val log: LogWrapper
) : MainContract.Presenter {

    override fun initialise() {
        log.tag = "MainPresenter"
        playerControls.initMediaRouteButton()
        playerControls.reset()
        if (!state.playServiceCheckDone) {
            view.checkPlayServices()
            state.playServiceCheckDone = true
        }
    }

    override fun onPlayServicesOk() {
        log.d("onPlayServicesOk()")
        state.playServicesAvailable = true
        if (!ytContextHolder.isCreated()) {
            initialiseCastContext()
        }
    }

    private fun initialiseCastContext() {
        ytContextHolder.create()

//        state.youtubePlayerContext = creator.createContext(chromeCastWrapper.getCastContext())
//        state.youtubePlayerContext?.playerUi = playerControls
//        log.d("initialiseCastContext()")
    }


    override fun onStart() {
        log.d("onStart()")
        ytServiceManager.stop()
        if (!ytContextHolder.isCreated() && state.playServicesAvailable) {
            initialiseCastContext()
        }
        ytContextHolder.get()?.playerUi = playerControls
//        if (ytServiceManager.isRunning()) {
//            log.d("onStart():svc.isRunning state_cxt:${state.youtubePlayerContext}, controls:${playerControls}")
//            state.youtubePlayerContext = ytServiceManager
//                .get()!!
//                .popYoutubeContext()!!
//            log.d("onStart():svc.isRunning from svc state_cxt:${state.youtubePlayerContext} existing controls:${state.youtubePlayerContext?.playerUi}")
//            state.youtubePlayerContext?.playerUi = playerControls
//            ytServiceManager.stop()
//        } else {
//            log.d("onStart():svc.isNotRunning state_cxt:${state.youtubePlayerContext}, controls:${playerControls}")
//            if (state.youtubePlayerContext == null && state.playServicesAvailable) {
//                initialiseCastContext()
//            }
//        }
    }

    override fun onStop() {
        log.d("onStop()")
        ytContextHolder.get()?.playerUi = null
        if (!view.isRecreating()) {
            ytServiceManager.start()
        }
    }

    override fun onDestroy() {
        if (ytContextHolder.isCreated() && !ytContextHolder.get()!!.isConnected()) {
            ytContextHolder.destroy()
        }

//        if (state.youtubePlayerContext!!.isConnected()) {
//            // move to service
//            log.d("onStop(): push cxt to svc state_cxt:${state.youtubePlayerContext}, controls:${playerControls}")
//            state.youtubePlayerContext!!.playerUi = null
//            val playerContext = state.youtubePlayerContext
//            state.youtubePlayerContext = null
////            ytServiceManager.start {// try moving to onStop before switch start might happen in same thread
////                ytServiceManager.get()!!.pushYoutubeContext(playerContext!!)
////            }
//        } else {
//            // kill everything
//            log.d("onStop(): destroy state_cxt:${state.youtubePlayerContext}, controls:${playerControls}")
//            state.youtubePlayerContext!!.playerUi = null
//            state.youtubePlayerContext!!.destroy()
//        }
    }

    companion object {
        val TAGP = "CuerLog"
        private val TAG = "$TAGP${MainPresenter::class.java.simpleName}"
    }
}