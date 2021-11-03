package uk.co.sentinelweb.cuer.app.ui.main

import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastServiceManager
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerServiceManager
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class MainPresenter(
    private val view: MainContract.View,
    private val state: MainContract.State,
    private val playerControls: PlayerContract.PlayerControls,
    private val ytServiceManager: YoutubeCastServiceManager,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val floatingPlayerServiceManager: FloatingPlayerServiceManager,
    private val castListener: FloatingPlayerCastListener,
    private val log: LogWrapper,
) : MainContract.Presenter {
    // todo add connection listener and close floating player if connectioned
    init {
        log.tag(this)
    }

    override fun initialise() {
        if (!state.playServiceCheckDone) {
            view.checkPlayServices()
            state.playServiceCheckDone = true
        }
        castListener.observeConnection()
    }

    override fun onPlayServicesOk() {
        log.d("onPlayServicesOk()")
        state.playServicesAvailable = true
        if (!ytContextHolder.isCreated()) {
            initialiseCastContext()
        }
    }

    override fun onDestroy() {

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
        if (!ytContextHolder.isConnected()) {
            floatingPlayerServiceManager.get()?.external?.mainPlayerControls = playerControls
        }
    }

    override fun onStop() {
        floatingPlayerServiceManager.get()?.external?.mainPlayerControls = null
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

}
