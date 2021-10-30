package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.common.skip.EmptySkipView
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipModelMapper
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipPresenter
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.ytplayer.PlayerModule

interface FloatingPlayerContract {

    interface Service {

    }

    interface Controller {
        fun initialise()
        fun destroy()
        fun handleAction(action: String?)
    }

    companion object {

        val serviceModule = module {
            factory { FloatingPlayerServiceManager(androidApplication(), get()) }
            factory { DisplayOverlayPermissionCheck() }
            scope(named<FloatingPlayerService>()) {
                scoped<Controller> {
                    FloatingPlayerController(getSource())
                }
                scoped<PlayerContract.PlaylistItemLoader> { NoItemLoader() }
                scoped {
                    PlayerController(
                        itemLoader = get(),
//                        storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        storeFactory = DefaultStoreFactory,
                        queueConsumer = get(),
                        queueProducer = get(),
                        modelMapper = get(),
                        coroutines = get(),
                        lifecycle = null,
                        skip = get(),
                        log = get(),
                        livePlaybackController = get(named(PlayerModule.LOCAL_PLAYER)),
                        mediaSessionManager = get()
                    )
                }
                scoped { FloatingWindowMviView(getSource(), get(), get()) }
                scoped { FloatingWindowManagement(getSource(), get()) }
                scoped<SkipContract.External> {
                    SkipPresenter(
                        view = EmptySkipView(),
                        state = SkipContract.State(),
                        log = get(),
                        mapper = SkipModelMapper(timeSinceFormatter = get(), res = get()),
                        prefsWrapper = get()
                    )
                }
//                scoped {
//                    PlayerControlsNotificationController(
//                        view = get(),
//                        context = androidApplication(),
//                        log = get(),
//                        state = get(),
//                        toastWrapper = get(),
//                        skipControl = get(),
//                        mediaSessionManager = get()
//                    )
//                }

//                scoped<PlayerControlsNotificationContract.External> {
//                    get<PlayerControlsNotificationController>()
//                }
//                scoped<PlayerControlsNotificationContract.Controller> {
//                    get<PlayerControlsNotificationController>()
//                }
//                scoped<PlayerControlsNotificationContract.View> {
//                    PlayerControlsNotificationMedia(
//                        service = getSource(),
//                        appState = get(),
//                        timeProvider = get(),
//                        log = get()
//                    )
//                }
//                scoped { PlayerControlsNotificationContract.State() }
            }
        }
    }
}