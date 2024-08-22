package uk.co.sentinelweb.cuer.app.service.cast

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.media.session.MediaButtonReceiver
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidApplication
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationCustom
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.cast.EmptyCastDialogLauncher
import uk.co.sentinelweb.cuer.app.ui.common.skip.EmptySkipView
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipPresenter
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.EmptyChromecastDialogWrapper
import uk.co.sentinelweb.cuer.app.util.extension.serviceScopeWithSource
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class CastService : Service(), CastServiceContract.Service, AndroidScopeComponent {

    override val scope: Scope by serviceScopeWithSource()
    private val controller: CastServiceContract.Controller by scope.inject()
    private val appState: CuerAppState by inject()
    private val log: LogWrapper by inject()
    private val notification: PlayerControlsNotificationContract.External by inject()

    override fun onCreate() {
        super.onCreate()
        log.tag(this)
        _instance = this
        log.d("Service created")
        // fixme why is this here?
        notification.setIcon(R.drawable.ic_play_yang_combined)
        controller.initialise()

    }

    override fun onDestroy() {
        super.onDestroy()
        log.d("Service destroyed")
        controller.destroy()
        scope.close()
        _instance = null
    }

    // media events are configured in manifest
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // this routes media buttons to the MediaSessionCompat
        if (intent?.action == Intent.ACTION_MEDIA_BUTTON) {
            MediaButtonReceiver.handleIntent(appState.mediaSession, intent)
        } else {
            controller.handleAction(intent?.action)
        }
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID: String = "cuer_yt_service"
        const val CHANNEL_NAME: String = "Cuer Youtube Service"

        private var _instance: CastService? = null
        fun instance(): CastService? = _instance

        val serviceModule = module {
            factory<CastServiceContract.Manager> { CastServiceManager(androidApplication()) }
            scope(named<CastService>()) {
                scoped<CastServiceContract.Controller> {
                    CastServiceController(
                        service = get<CastService>(),
//                        ytContextHolder = get(),
                        notification = get(),
//                        chromeCastWrapper = get(),
                        castController = get(),
                        log = get(),
                    )
                }
                scoped {
                    PlayerControlsNotificationController(
                        view = get(),
                        context = androidApplication(),
                        log = get(),
                        state = get(),
                        toastWrapper = get(),
                        skipControl = get(),
                        mediaSessionManager = get(),
                        timeProvider = get()
                    )
                }
                scoped<PlayerControlsNotificationContract.External> {
                    get<PlayerControlsNotificationController>()
                }
                scoped<PlayerContract.PlayerControls> {
                    get<PlayerControlsNotificationController>()
                }
                scoped<SkipContract.External> {
                    SkipPresenter(
                        view = EmptySkipView(),
                        state = SkipContract.State(),
                        log = get(),
                        mapper = get(),
                        prefsWrapper = get()
                    )
                }
                scoped<PlayerControlsNotificationContract.View> {
//                    PlayerControlsNotificationMedia(
//                        service = get<CastService>(),
//                        appState = get(),
//                        timeProvider = get(),
//                        log = get(),
//                        launchClass = MainActivity::class.java
//                    )
                    PlayerControlsNotificationCustom(
                        service = get<CastService>(),
                        appState = get(),
                        timeProvider = get(),
                        log = get(),
                        launchClass = MainActivity::class.java,
                        playerUiMapper = get()
                    )
                }
                scoped { PlayerControlsNotificationContract.State() }
                scoped {
                    CastController(
                        cuerCastPlayerWatcher = get(),
                        chromeCastHolder = get(),
                        chromeCastDialogWrapper = EmptyChromecastDialogWrapper(),
                        chromeCastWrapper = get(),
                        floatingManager = get(),
                        playerControls = get(),
                        castDialogLauncher = EmptyCastDialogLauncher(),
                        ytServiceManager = get(),
                        coroutines = get(),
                        log = get(),
                    )
                }
            }
        }
    }
}
