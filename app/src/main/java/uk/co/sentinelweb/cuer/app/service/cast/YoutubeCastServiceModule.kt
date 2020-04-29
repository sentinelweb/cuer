package uk.co.sentinelweb.cuer.app.service.cast

import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotification
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationPresenter
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationState

object YoutubeCastServiceModule {

    val serviceModule = module {
        factory { YoutubeCastServiceManager(androidApplication()) }
        scope(named<YoutubeCastService>()) {
            scoped { YoutubeCastServiceController(getSource(), get(), get(), get()) }
            scoped { YoutubeCastServiceState() }
            scoped { PlayerControlsNotificationPresenter(get(), get(), get()) }
            scoped<PlayerControlsNotificationContract.PresenterExternal> {
                get<PlayerControlsNotificationPresenter>()
            }
            scoped<PlayerControlsNotificationContract.Presenter> {
                get<PlayerControlsNotificationPresenter>()
            }
            scoped<PlayerControlsNotificationContract.View> {
                PlayerControlsNotification(
                    getSource(),
                    get()
                )
            }
            scoped { PlayerControlsNotificationState() }
        }
    }
}