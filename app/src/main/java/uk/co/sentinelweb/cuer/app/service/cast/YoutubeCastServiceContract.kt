package uk.co.sentinelweb.cuer.app.service.cast

import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationMedia
import uk.co.sentinelweb.cuer.app.ui.common.skip.EmptySkipView
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipModelMapper
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipPresenter
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity

interface YoutubeCastServiceContract {

    interface Service {
        fun stopSelf()
    }

    interface Controller {
        fun initialise()
        fun handleAction(action: String?)
        fun destroy()
    }

    companion object {

        val serviceModule = module {
            factory { YoutubeCastServiceManager(androidApplication()) }
            scope(named<YoutubeCastService>()) {
                scoped<Controller> {
                    YoutubeCastServiceController(get<YoutubeCastService>(), get(), get(), get(), get(), get())
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
                        res = get()
                    )
                }
                scoped<SkipContract.External> {
                    SkipPresenter(
                        view = EmptySkipView(),
                        state = SkipContract.State(),
                        log = get(),
                        mapper = SkipModelMapper(timeSinceFormatter = get(), res = get()),
                        prefsWrapper = get()
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
                        service = get<YoutubeCastService>(),
                        appState = get(),
                        timeProvider = get(),
                        log = get(),
                        launchClass = MainActivity::class.java
                    )
                }
                scoped { PlayerControlsNotificationContract.State() }
            }
        }
    }
}