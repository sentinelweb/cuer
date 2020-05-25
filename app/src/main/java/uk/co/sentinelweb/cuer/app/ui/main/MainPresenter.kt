package uk.co.sentinelweb.cuer.app.ui.main

import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastServiceManager
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

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
    }


    override fun onStart() {
        log.d("onStart()")
        ytServiceManager.stop()
        if (!ytContextHolder.isCreated() && state.playServicesAvailable) {
            initialiseCastContext()
        }
        ytContextHolder.playerUi = playerControls
    }

    override fun onStop() {
        log.d("onStop()")
        ytContextHolder.playerUi = null
        if (!view.isRecreating()) {
            if (ytContextHolder.isCreated() && !ytContextHolder.isConnected()) {
                ytContextHolder.destroy()
            } else {
                ytServiceManager.start()
            }
        }
    }

    override fun restartYtCastContext() {
        ytContextHolder.destroy()
        ytContextHolder.create()
    }

    override fun onDestroy() = Unit

    companion object {
        val TAGP = "CuerLog"
    }

}