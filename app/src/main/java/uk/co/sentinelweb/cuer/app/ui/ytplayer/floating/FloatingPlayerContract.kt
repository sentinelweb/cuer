package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import android.content.Intent
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationMedia
import uk.co.sentinelweb.cuer.app.ui.common.skip.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.player.PlayerListener
import uk.co.sentinelweb.cuer.app.ui.player.PlayerStoreFactory
import uk.co.sentinelweb.cuer.app.ui.ytplayer.PlayerModule
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_portrait.AytPortraitActivity

interface FloatingPlayerContract {

    interface Service {
        val external: External
        fun stopSelf()
    }

    interface Controller {
        val external: External
        fun initialise()
        fun destroy()
        fun handleAction(intent: Intent)
    }

    interface External {
        var mainPlayerControls: PlayerContract.PlayerControls?
    }

    companion object {
        @ExperimentalCoroutinesApi
        val serviceModule = module {
            factory { FloatingPlayerServiceManager(androidApplication(), get()) }
            factory { DisplayOverlayPermissionCheck() }
            scope(named<FloatingPlayerService>()) {
                scoped<Controller> {
                    FloatingPlayerController(
                        service = getSource(),
                        playerController = get(),
                        playerMviViw = get(),
                        windowManagement = get(),
                        aytViewHolder = get(),
                        log = get(),
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
                        playControls = get(),
                        store = get()
                    )
                }
                scoped {
                    PlayerStoreFactory(
                        // storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        storeFactory = DefaultStoreFactory,
                        itemLoader = get(),
                        queueConsumer = get(),
                        queueProducer = get(),
                        skip = get(),
                        coroutines = get(),
                        log = get(),
                        livePlaybackController = get(named(PlayerModule.LOCAL_PLAYER)),
                        mediaSessionManager = get(),
                        playerControls = get(),
                    ).create()
                }
                scoped { PlayerListener(get(), get()) }
                scoped { FloatingWindowMviView(getSource(), get(), get(), get()) }
                scoped { FloatingWindowManagement(getSource(), get(), get(), get()) }
                scoped<SkipContract.External> {
                    SkipPresenter(
                        view = EmptySkipView(),
                        state = SkipContract.State(),
                        log = get(),
                        mapper = SkipModelMapper(timeSinceFormatter = get(), res = get()),
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
                        mediaSessionManager = get()
                    )
                }
                scoped<PlayerControlsNotificationContract.External> {
                    get<PlayerControlsNotificationController>()
                }
                scoped<PlayerControlsNotificationContract.Controller> {
                    get<PlayerControlsNotificationController>()
                }
                scoped<PlayerControlsNotificationContract.View> {
                    PlayerControlsNotificationMedia(
                        service = getSource(),
                        appState = get(),
                        timeProvider = get(),
                        log = get(),
                        launchClass = AytPortraitActivity::class.java
                    )
                }
                scoped { PlayerControlsNotificationContract.State() }
            }
        }
    }
}