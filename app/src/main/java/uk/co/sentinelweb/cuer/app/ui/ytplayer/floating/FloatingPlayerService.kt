package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.media.session.MediaButtonReceiver
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidApplication
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationCustom
import uk.co.sentinelweb.cuer.app.ui.common.skip.EmptySkipPresenter
import uk.co.sentinelweb.cuer.app.ui.common.skip.EmptySkipView
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipPresenter
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.player.PlayerStoreFactory
import uk.co.sentinelweb.cuer.app.ui.ytplayer.PlayerModule.LOCAL_PLAYER
import uk.co.sentinelweb.cuer.app.util.extension.serviceScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.NotificationWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class FloatingPlayerService : Service(), FloatingPlayerContract.Service, AndroidScopeComponent {

    override val scope: Scope by serviceScopeWithSource()
    private val controller: FloatingPlayerContract.Controller by scope.inject()
    private val toastWrapper: ToastWrapper by inject()
    private val notificationWrapper: NotificationWrapper by inject()
    private val appState: CuerAppState by inject()
    private val log: LogWrapper by inject()

    override val external: FloatingPlayerContract.External
        get() = controller.external

    override fun onCreate() {
        super.onCreate()
        log.tag(this)
        _instance = this
        log.d("Service created")
        appState.floatingNotificationChannelId =
            notificationWrapper.createChannelId(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MAX)
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
            intent?.apply { controller.handleAction(intent) }
        }
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? = null

    companion object {
        const val ACTION_INIT: String = "init"
        const val ACTION_PLAY_ITEM: String = "playitem"

        const val CHANNEL_ID: String = "cuer_floating_service"
        const val CHANNEL_NAME: String = "Cuer Floating Service"

        private var _instance: FloatingPlayerService? = null
        fun instance(): FloatingPlayerService? = _instance

        @ExperimentalCoroutinesApi
        val serviceModule = module {
            factory { FloatingPlayerServiceManager(androidApplication(), get()) }
            factory<FloatingPlayerContract.Manager> { get<FloatingPlayerServiceManager>() }
            factory { DisplayOverlayPermissionCheck() }
            scope(named<FloatingPlayerService>()) {
                scoped<FloatingPlayerContract.Controller> {
                    FloatingPlayerController(
                        service = get(),
                        playerController = get(),
                        playerMviViw = get(),
                        windowManagement = get(),
                        aytViewHolder = get(),
                        log = get(),
                        screenStateReceiver = get(),
                        toastWrapper = get(),
                        multiPrefs = get(),
                    )
                }
                scoped<PlayerContract.PlaylistItemLoader> { NoItemLoader() }
                scoped {
                    PlayerController(
                        queueConsumer = get(),
                        modelMapper = get(),
                        coroutines = get(),
                        lifecycle = null,
                        log = get(),
                        mediaSessionListener = get(),
                        store = get(),
                        playSessionListener = get()
                    )
                }
                scoped {
                    PlayerStoreFactory(
                        // storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        storeFactory = DefaultStoreFactory(),
                        itemLoader = get(),
                        queueConsumer = get(),
                        queueProducer = get(),
                        skip = get(),
                        coroutines = get(),
                        log = get(),
                        livePlaybackController = get(named(LOCAL_PLAYER)),
                        mediaSessionManager = get(),
                        mediaSessionListener = get(),
                        mediaOrchestrator = get(),
                        playlistItemOrchestrator = get(),
                        playerSessionManager = get(),
                        playerSessionListener = get(),
                        config = PlayerContract.PlayerConfig(100f),
                        prefs = get(),
                    ).create()
                }
                scoped { FloatingWindowMviView(get(), get(), get(), get(), get()) }
                scoped { FloatingWindowManagement(get(), get(), get(), get(), get()) }
                scoped<SkipContract.External> {
                    SkipPresenter(
                        view = EmptySkipView(),
                        state = SkipContract.State(),
                        log = get(),
                        mapper = get(),
                        prefsWrapper = get()
                    )
                }
                scoped {
                    PlayerControlsNotificationController(
                        view = get(),
                        context = androidApplication(),
                        log = get(),
                        state = get(),
                        toastWrapper = get(),
                        skipControl = EmptySkipPresenter(),
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
                scoped<PlayerControlsNotificationContract.View> {
//                    PlayerControlsNotificationMedia(
//                        service = get(),
//                        appState = get(),
//                        timeProvider = get(),
//                        log = get(),
//                        launchClass = AytPortraitActivity::class.java
//                    )
                    PlayerControlsNotificationCustom(
                        service = get(),
                        appState = get(),
                        timeProvider = get(),
                        log = get(),
                        launchClass = MainActivity::class.java,
                        playerUiMapper = get(),
                        channelId = get<CuerAppState>().floatingNotificationChannelId,
                        showVolumeControls = false,
                    )

                }
                scoped { PlayerControlsNotificationContract.State() }
            }
        }
    }
}
