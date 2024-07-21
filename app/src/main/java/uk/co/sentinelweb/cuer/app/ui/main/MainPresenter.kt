package uk.co.sentinelweb.cuer.app.ui.main

import uk.co.sentinelweb.cuer.app.backup.AutoBackupFileExporter
import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastServiceManager
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerContract
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromeCastContract
import uk.co.sentinelweb.cuer.app.util.permission.NotificationPermissionCheckDialog
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class MainPresenter(
    private val view: MainContract.View,
    private val state: MainContract.State,
    private val ytServiceManager: YoutubeCastServiceManager,
    private val ytContextHolder: ChromeCastContract.PlayerContextHolder,
    private val floatingPlayerServiceManager: FloatingPlayerContract.Manager,
    private val castListener: FloatingPlayerCastListener,
    private val log: LogWrapper,
    private val autoBackupFileExporter: AutoBackupFileExporter,
    private val notificationPermissionCheckDialog: NotificationPermissionCheckDialog,
    private val castController: CastController,
) : MainContract.Presenter {

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
            //view.playerControls.initMediaRouteButton()
            view.playerControls.reset()
            state.playControlsInit = true
        }
        castController.checkCastConnection()

        autoBackupFileExporter.attemptAutoBackup { result -> view.promptToBackup(result) }

        notificationPermissionCheckDialog.checkToShow()
    }

    override fun onStop() {
        floatingPlayerServiceManager.get()?.external?.mainPlayerControls = null
        ytContextHolder.playerUi = null
        if (!view.isRecreating()) {
            // todo manage this in castcontroller
            if (ytContextHolder.isCreated() && !ytContextHolder.isConnected()) {
                ytContextHolder.destroy()
            } else {
                ytServiceManager.start()
            }
        }
    }

//    override fun restartYtCastContext() {
//        ytContextHolder.destroy()
//        ytContextHolder.create()
//    }

}
