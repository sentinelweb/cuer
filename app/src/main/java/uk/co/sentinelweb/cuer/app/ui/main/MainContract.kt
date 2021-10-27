package uk.co.sentinelweb.cuer.app.ui.main

import androidx.lifecycle.ViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.inteface.CommitHost
import uk.co.sentinelweb.cuer.app.ui.common.inteface.EmptyCommitHost
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationMapper
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerFragment
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditContract
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper

interface MainContract {
    interface Presenter {
        fun initialise()
        //fun startServer()
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

    interface PlayerViewControl {
        fun showPlayer()
        fun hidePlayer()
    }

    data class State constructor(
        var playServicesAvailable: Boolean = false,
        var playServiceCheckDone: Boolean = false,
        var playControlsInit: Boolean = false,
    ) : ViewModel()

    companion object {
        @JvmStatic
        val activityModule = module {
            scope(named<MainActivity>()) {
                scoped<View> { getSource() }
                scoped<PlayerViewControl> { getSource() }
                scoped<NavigationProvider> { getSource() }
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
                scoped { navigationMapper(false, getSource()) }
                scoped { YoutubeJavaApiWrapper(getSource()) }
                viewModel { State() }
                scoped<SnackbarWrapper> { AndroidSnackbarWrapper(getSource(), get()) }
                scoped<PlaylistItemEditContract.DoneNavigation> { getSource() }
                // fragment interactions
                scoped<PlaylistContract.Interactions?> { null }
                scoped<CommitHost> { EmptyCommitHost() }
            }
        }
    }
}