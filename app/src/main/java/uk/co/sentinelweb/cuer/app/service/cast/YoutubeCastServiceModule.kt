package uk.co.sentinelweb.cuer.app.service.cast

import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotification
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationMedia
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationState

object YoutubeCastServiceModule {

    val serviceModule = module {
        factory { YoutubeCastServiceManager(androidApplication()) }
        scope(named<YoutubeCastService>()) {
            scoped<YoutubeCastServiceContract.Controller> {
                YoutubeCastServiceController(getSource(), get(), get(), get(), get(), get())
            }
            scoped {
                PlayerControlsNotification(get(), get(), get(), get(), androidApplication())
            }
            scoped<PlayerControlsNotificationContract.External> {
                get<PlayerControlsNotification>()
            }
            scoped<PlayerControlsNotificationContract.Presenter> {
                get<PlayerControlsNotification>()
            }
            scoped<PlayerControlsNotificationContract.View> {
                PlayerControlsNotificationMedia(
                    service = getSource(),
                    appState = get(),
                    timeProvider = get(),
                    log = get()
                )
            }
            scoped { PlayerControlsNotificationState() }
        }
    }
}