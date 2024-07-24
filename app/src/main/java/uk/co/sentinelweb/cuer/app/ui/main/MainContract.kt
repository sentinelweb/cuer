package uk.co.sentinelweb.cuer.app.ui.main

import androidx.lifecycle.ViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.backup.AutoBackupFileExporter
import uk.co.sentinelweb.cuer.app.ui.cast.CastContract
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.cast.CastDialogLauncher
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.inteface.CommitHost
import uk.co.sentinelweb.cuer.app.ui.common.inteface.EmptyCommitHost
import uk.co.sentinelweb.cuer.app.ui.common.navigation.DoneNavigation
import uk.co.sentinelweb.cuer.app.ui.common.navigation.LinkNavigator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerFragment
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogContract
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogLauncher
import uk.co.sentinelweb.cuer.app.ui.share.ShareNavigationHack
import uk.co.sentinelweb.cuer.app.util.chromecast.CastDialogWrapper
import uk.co.sentinelweb.cuer.app.util.chromecast.CuerSimpleVolumeController
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromecastContract
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
        //fun restartYtCastContext()
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
                        ytContextHolder = get(),
                        log = get(),
                        floatingPlayerServiceManager = get(),
                        floatingPlayerCastListener = get(),
                        autoBackupFileExporter = get(),
                        notificationPermissionCheckDialog = get(),
                        castController = get()
                    )
                }
                scoped {
                    (get<MainActivity>()
                        .supportFragmentManager
                        .findFragmentById(R.id.cast_player_fragment) as CastPlayerFragment).playerControls
                }
                scoped { navigationRouter(isFragment = false, sourceActivity = get<MainActivity>()) }
                viewModel { State() }
                scoped<SnackbarWrapper> { AndroidSnackbarWrapper(a = get<MainActivity>(), res = get()) }
                scoped {
                    FloatingPlayerCastListener(
                        activity = get(),
                        wrapper = get(),
                        floatingPlayerServiceManager = get()
                    )
                }
                scoped { AlertDialogCreator(context = get<MainActivity>(), strings = get()) }
                scoped {
                    LinkNavigator(
                        navRouter = get(),
                        linkScanner = get(),
                        shareNavigationHack = get(),
                        playlistItemOrchestrator = get(),
                        playlistOrchestrator = get(),
                        coroutines = get(),
                        isMain = true
                    )
                }
                scoped { EmailWrapper(activity = get<MainActivity>()) }
                scoped { AndroidShareWrapper(activity = get<MainActivity>()) }

                // ALL SHARE HACKS
                scoped<DoneNavigation> { MainDoneNavigation(mainActivity = get<MainActivity>()) }
                scoped<CommitHost> { EmptyCommitHost() }
                scoped { ShareNavigationHack() }

                scoped<NavigationProvider> {
                    MainNavigationProvider(
                        mainActivity = get<MainActivity>(),
                        navRouter = get(),
                        log = get()
                    )
                }
                scoped {
                    NotificationPermissionCheckDialog(
                        activity = get<MainActivity>(),
                        notificationPermissionCheck = get(),
                        log = get()
                    )
                }
                scoped {
                    CastController(
                        cuerCastPlayerWatcher = get(),
                        chromeCastHolder = get(),
                        chromeCastDialogWrapper = get(),
                        chromeCastWrapper = get(),
                        floatingManager = get(),
                        playerControls = get(),
                        castDialogLauncher = get(),
                        ytServiceManager = get(), log = get(),
                    )
                }
                scoped<CastContract.CastDialogLauncher> { CastDialogLauncher(activity = get<MainActivity>()) }
                scoped<ChromecastContract.DialogWrapper> {
                    CastDialogWrapper(
                        activity = get<MainActivity>(),
                        chromeCastWrapper = get()
                    )
                }
                scoped<RemotesDialogContract.Launcher> { RemotesDialogLauncher(activity = get<MainActivity>()) }
                scoped { CuerSimpleVolumeController(castController = get(), log = get()) }
            }
        }
    }
}