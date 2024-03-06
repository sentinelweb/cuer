package uk.co.sentinelweb.cuer.app.ui.settings

import android.content.Context
import kotlinx.datetime.toJavaInstant
import org.koin.core.component.KoinComponent
import uk.co.sentinelweb.cuer.app.BuildConfig
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringResource
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingFragment
import uk.co.sentinelweb.cuer.app.ui.upcoming.UpcomingContract
import uk.co.sentinelweb.cuer.app.usecase.EmailUseCase
import uk.co.sentinelweb.cuer.app.usecase.ShareUseCase
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseWrapper
import uk.co.sentinelweb.cuer.app.work.WorkManagerInteractor
import uk.co.sentinelweb.cuer.app.work.worker.UpcomingVideosCheckWorker
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class PrefRootPresenter constructor(
    private val view: PrefRootContract.View,
    private val state: PrefRootContract.State,
    private val timeProvider: TimeProvider,
    private val firebaseWrapper: FirebaseWrapper,
    private val log: LogWrapper,
//    private val remoteServiceManger: RemoteServerServiceManager,
//    private val coroutines: CoroutineContextProvider,
    private val emailUseCase: EmailUseCase,
    private val shareUseCase: ShareUseCase,
) : PrefRootContract.Presenter, KoinComponent {

    override fun sendDebugReports() {
        if (firebaseWrapper.hasUnsentReports()) {
            firebaseWrapper.sendUnsentReports()
            view.showMessage("Sent reports")
        } else {
            view.showMessage("No reports sent")
        }
        state.lastDebugSent = timeProvider.instant().toJavaInstant()
    }

//    override fun toggleRemoteService() {
//        if (remoteServiceManger.isRunning()) {
//            remoteServiceManger.stop()
//            view.setRemoteServiceRunning(false, null)
//        } else {
//            coroutines.mainScope.launch {
//                remoteServiceManger.start()
//                while (remoteServiceManger.getService()?.isServerStarted != true) {// fixme limit?
//                    delay(20)
//                }
//                val http = remoteServiceManger.getService()?.localNode?.http()
//                log.d("isRunning ${remoteServiceManger.isRunning()} svc: ${remoteServiceManger.getService()} address: $http")
//                view.setRemoteServiceRunning(true, http)
//            }
//        }
//    }

    override fun initialisePrefs() {
//        view.setRemoteServiceRunning(remoteServiceManger.isRunning(), remoteServiceManger.getService()?.localNode?.http())
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

    override fun test() {
        getKoin().get<WorkManagerInteractor>()
            .checkStatus(getKoin().get<Context>(), UpcomingVideosCheckWorker.WORK_NAME)
        getKoin().get<UpcomingContract.Presenter>().checkForUpcomingEpisodes(30)
    }
}