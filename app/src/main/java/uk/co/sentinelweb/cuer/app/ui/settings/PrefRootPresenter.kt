package uk.co.sentinelweb.cuer.app.ui.settings

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaInstant
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServiceManager
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class PrefRootPresenter constructor(
    private val view: PrefRootContract.View,
    private val state: PrefRootContract.State,
    private val timeProvider: TimeProvider,
    private val firebaseWrapper: FirebaseWrapper,
    private val log: LogWrapper,
    private val remoteServiceManger: RemoteServiceManager,
    private val coroutines: CoroutineContextProvider
) : PrefRootContract.Presenter {

    override fun sendDebugReports() {
        if (firebaseWrapper.hasUnsentReports()) {
            firebaseWrapper.sendUnsentReports()
            view.showMessage("Sent reports")
        } else {
            view.showMessage("No reports sent")
        }
        state.lastDebugSent = timeProvider.instant().toJavaInstant()
    }

    override fun toggleRemoteService() {
        if (remoteServiceManger.isRunning()) {
            remoteServiceManger.stop()
            view.setRemoteServiceRunning(false, null)
        } else {
            coroutines.mainScope.launch {
                remoteServiceManger.start()
                while (remoteServiceManger.get()?.isServerStarted != true) {
                    delay(20)
                }
                log.d("isRunning ${remoteServiceManger.isRunning()} svc: ${remoteServiceManger.get()} address: ${remoteServiceManger.get()?.address}")
                view.setRemoteServiceRunning(true, remoteServiceManger.get()?.address)
            }
        }
    }

    override fun initialisePrefs() {
        view.setRemoteServiceRunning(remoteServiceManger.isRunning(), remoteServiceManger.get()?.address)
    }
}