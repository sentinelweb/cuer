package uk.co.sentinelweb.cuer.app.ui.settings

import kotlinx.datetime.toJavaInstant
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseWrapper
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class PrefRootPresenter constructor(
    private val view: PrefRootContract.View,
    private val state: PrefRootContract.State,
    private val timeProvider: TimeProvider,
    private val firebaseWrapper: FirebaseWrapper,
    private val log: LogWrapper
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
}