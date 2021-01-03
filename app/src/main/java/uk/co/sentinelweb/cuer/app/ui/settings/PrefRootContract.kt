package uk.co.sentinelweb.cuer.app.ui.settings

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import java.time.Instant

interface PrefRootContract {
    interface Presenter {
        fun sendDebugReports()
    }

    interface View {
        fun showMessage(msg: String)
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
                        timeProvider = get()
                    )
                }
                scoped { SnackbarWrapper((getSource() as Fragment).requireActivity()) }
                viewModel { State() }
            }
        }
    }
}