package uk.co.sentinelweb.cuer.app.ui.main

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerFragment
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper

interface MainContract {
    interface Presenter {
        fun initialise()
        fun onStart()
        fun onStop()
        fun onPlayServicesOk()
        fun onDestroy()
        fun restartYtCastContext()
    }

    interface View {
        fun checkPlayServices()
        fun isRecreating(): Boolean
        fun showMessage(msg: String)
    }

    data class State constructor(
        var playServicesAvailable: Boolean = false,
        var playServiceCheckDone: Boolean = false,
        var playControlsInit: Boolean = false
    ) : ViewModel()

    companion object {
        @JvmStatic
        val activityModule = module {
            scope(named<MainActivity>()) {
                scoped<View> { getSource() }
                scoped<Presenter> {
                    MainPresenter(
                        view = get(),
                        state = get(),
                        playerControls = get(),
                        ytServiceManager = get(),
                        ytContextHolder = get(),
                        log = get()
                    )
                }
                scoped {
                    (getSource<MainActivity>()
                        .supportFragmentManager
                        .findFragmentById(R.id.cast_player_fragment) as CastPlayerFragment).playerControls
                }
                scoped {
                    NavigationMapper(
                        activity = getSource(),
                        toastWrapper = get(),
                        ytJavaApi = get(),
                        navController = (getSource<AppCompatActivity>()
                            .supportFragmentManager
                            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                            .navController,
                        log = get()
                    )
                }
                scoped { YoutubeJavaApiWrapper(getSource()) }
                viewModel { State() }
                scoped { SnackbarWrapper(getSource()) }
            }
        }
    }
}