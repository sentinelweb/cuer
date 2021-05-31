package uk.co.sentinelweb.cuer.app.ui.main

import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastServiceManager
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServiceManager
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class MainPresenter(
    private val view: MainContract.View,
    private val state: MainContract.State,
    private val playerControls: CastPlayerContract.PlayerControls,
    private val ytServiceManager: YoutubeCastServiceManager,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val log: LogWrapper,
    private val remoteServiceManger: RemoteServiceManager
) : MainContract.Presenter {

    init {
        log.tag(this)
    }

    override fun initialise() {
        if (!state.playServiceCheckDone) {
            view.checkPlayServices()
            state.playServiceCheckDone = true
        }
    }

    override fun startServer() {
        remoteServiceManger.start()
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
        if (!state.playControlsInit) {
            playerControls.initMediaRouteButton()
            playerControls.reset()
            state.playControlsInit = true
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

    override fun onDestroy() {
        remoteServiceManger.stop()
    }
}
