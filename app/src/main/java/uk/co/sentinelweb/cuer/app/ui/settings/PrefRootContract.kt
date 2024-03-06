package uk.co.sentinelweb.cuer.app.ui.settings

import androidx.lifecycle.ViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.usecase.EmailUseCase
import uk.co.sentinelweb.cuer.app.usecase.ShareUseCase
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import java.time.Instant

interface PrefRootContract {
    interface Presenter {
        fun sendDebugReports()
//        fun toggleRemoteService()
        fun initialisePrefs()
        fun onFeedback()
        fun onShare()
        fun resetOnboarding()
        fun launchUsability()
        fun launchBymcDonate()
        fun test()
    }

    interface View {
        fun showMessage(msg: String)

        //        fun setRemoteServiceRunning(running: Boolean, address: String?)
        fun setVersion(versionString: String)
        fun sendEmail(data: EmailUseCase.Data)
        fun launchShare(data: ShareUseCase.Data)
        fun launchLink(url: String)
    }

    data class State constructor(
        var lastDebugSent: Instant? = null
    ) : ViewModel()

    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<PrefRootFragment>()) {
                scoped<View> { get<PrefRootFragment>() }
                scoped<Presenter> {
                    PrefRootPresenter(
                        view = get(),
                        state = get(),
                        log = get(),
                        firebaseWrapper = get(),
                        timeProvider = get(),
//                        remoteServiceManger = get(),
//                        coroutines = get(),
                        emailUseCase = get(),
                        shareUseCase = get(),
                    )
                }
                scoped<SnackbarWrapper> {
                    AndroidSnackbarWrapper(
                        this.getFragmentActivity(),
                        get()
                    )
                }
                viewModel { State() }
            }
        }
    }
}