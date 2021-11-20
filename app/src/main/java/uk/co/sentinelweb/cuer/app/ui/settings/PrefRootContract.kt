package uk.co.sentinelweb.cuer.app.ui.settings

import androidx.lifecycle.ViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import java.time.Instant

interface PrefRootContract {
    interface Presenter {
        fun sendDebugReports()
        fun toggleRemoteService()
        fun initialisePrefs()
    }

    interface View {
        fun showMessage(msg: String)
        fun setRemoteServiceRunning(running: Boolean, address: String?)
    }

    data class State constructor(
        var lastDebugSent: Instant? = null
    ) : ViewModel()

    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<PrefRootFragment>()) {
                scoped<View> { getSource() }
                scoped<Presenter> {
                    PrefRootPresenter(
                        view = get(),
                        state = get(),
                        log = get(),
                        firebaseWrapper = get(),
                        timeProvider = get(),
                        remoteServiceManger = get(),
                        coroutines = get()
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