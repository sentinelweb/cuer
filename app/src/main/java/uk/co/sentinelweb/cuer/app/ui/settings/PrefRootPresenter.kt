package uk.co.sentinelweb.cuer.app.ui.settings

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaInstant
import uk.co.sentinelweb.cuer.app.BuildConfig
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServiceManager
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringResource
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingFragment
import uk.co.sentinelweb.cuer.app.usecase.EmailUseCase
import uk.co.sentinelweb.cuer.app.usecase.ShareUseCase
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
    private val coroutines: CoroutineContextProvider,
    private val emailUseCase: EmailUseCase,
    private val shareUseCase: ShareUseCase,
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
        view.setVersion("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
    }

    override fun onFeedback() {
        view.sendEmail(emailUseCase.makeFeedbackEmail())
    }

    override fun onShare() {
        view.launchShare(shareUseCase.shareApp())
    }

    override fun resetOnboarding() {
        OnboardingFragment.setOnboardingState(isShown = false)
//        System.exit(0)
    }

    override fun launchUsability() {
        view.launchLink(StringResource.url_usability_form.default)
    }

    override fun launchBymcDonate() {
        view.launchLink(StringResource.url_bymc_donate_form.default)
    }
}