package uk.co.sentinelweb.cuer.app.ui.main

import uk.co.sentinelweb.cuer.app.backup.AutoBackupFileExporter
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerContract
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromecastContract
import uk.co.sentinelweb.cuer.app.util.permission.NotificationPermissionCheckDialog
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class MainPresenter(
    private val view: MainContract.View,
    private val state: MainContract.State,
    private val ytContextHolder: ChromecastContract.PlayerContextHolder,
    private val floatingPlayerServiceManager: FloatingPlayerContract.Manager,
    private val floatingPlayerCastListener: FloatingPlayerCastListener,
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
        floatingPlayerCastListener.observeConnection()
    }

    override fun onPlayServicesOk() {
        log.d("onPlayServicesOk()")
        state.playServicesAvailable = true
//        if (!ytContextHolder.isCreated()) {
//            initialiseCastContext()
//        }
    }

    override fun onDestroy() {

    }

//    private fun initialiseCastContext() {
//        ytContextHolder.create(view.playerControls)
//    }

    override fun onStart() {
        log.d("onStart()")
//        if (!ytContextHolder.isCreated() && state.playServicesAvailable) {
//            initialiseCastContext()
//        }
        if (!state.playControlsInit) {
            // view.playerControls.initMediaRouteButton()
            view.playerControls.reset()
            state.playControlsInit = true
        }
        castController.checkCastConnectionToActivity()

        autoBackupFileExporter.attemptAutoBackup { result -> view.promptToBackup(result) }

        notificationPermissionCheckDialog.checkToShow()
    }

    override fun onStop() {
        floatingPlayerServiceManager.get()?.external?.mainPlayerControls = null
        ytContextHolder.mainPlayerControls = null
        if (!view.isRecreating()) {
            castController.switchToService()
        }
    }

//    override fun restartYtCastContext() {
//        ytContextHolder.destroy()
//        ytContextHolder.create()
//    }

}
