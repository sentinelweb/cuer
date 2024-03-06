package uk.co.sentinelweb.cuer.app.ui.main

import androidx.lifecycle.ViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.backup.AutoBackupFileExporter
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.inteface.CommitHost
import uk.co.sentinelweb.cuer.app.ui.common.inteface.EmptyCommitHost
import uk.co.sentinelweb.cuer.app.ui.common.navigation.DoneNavigation
import uk.co.sentinelweb.cuer.app.ui.common.navigation.LinkNavigator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerFragment
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.share.ShareNavigationHack
import uk.co.sentinelweb.cuer.app.util.permission.NotificationPermissionCheckDialog
import uk.co.sentinelweb.cuer.app.util.share.AndroidShareWrapper
import uk.co.sentinelweb.cuer.app.util.share.EmailWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper

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
        val playerControls: PlayerContract.PlayerControls
        fun checkPlayServices()
        fun isRecreating(): Boolean
        fun showMessage(msg: String)
        fun promptToBackup(result: AutoBackupFileExporter.BackupResult)
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
                scoped<View> { get<MainActivity>() }
                scoped<PlayerViewControl> { get<MainActivity>() }
                scoped<Presenter> {
                    MainPresenter(
                        view = get(),
                        state = get(),
                        ytServiceManager = get(),
                        ytContextHolder = get(),
                        log = get(),
                        floatingPlayerServiceManager = get(),
                        castListener = get(),
                        autoBackupFileExporter = get(),
                        notificationPermissionCheckDialog = get(),
                    )
                }
                scoped {
                    (get<MainActivity>()
                        .supportFragmentManager
                        .findFragmentById(R.id.cast_player_fragment) as CastPlayerFragment).playerControls
                }
                scoped { navigationRouter(false, get<MainActivity>()) }
                viewModel { State() }
                scoped<SnackbarWrapper> { AndroidSnackbarWrapper(get<MainActivity>(), get()) }
                // scoped<PlaylistContract.Interactions?> { null }
                scoped { FloatingPlayerCastListener(get(), get(), get()) }
                scoped { AlertDialogCreator(get<MainActivity>(), get()) }
                scoped { LinkNavigator(get(), get(), get(), get(), get(), get(), true) }
                scoped { EmailWrapper(get<MainActivity>()) }
                scoped { AndroidShareWrapper(get<MainActivity>()) }

                // ALL SHARE HACKS
                scoped<DoneNavigation> { MainDoneNavigation(get<MainActivity>()) }
                scoped<CommitHost> { EmptyCommitHost() }
                scoped { ShareNavigationHack() }
                scoped<NavigationProvider> { MainNavigationProvider(get<MainActivity>(), get(), get()) }
                scoped { NotificationPermissionCheckDialog(get<MainActivity>(), get()) }
            }
        }
    }
}