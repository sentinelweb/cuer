package uk.co.sentinelweb.cuer.app.service.cast

import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationMedia
import uk.co.sentinelweb.cuer.app.ui.common.skip.EmptySkipView
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipPresenter
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences

object YoutubeCastServiceModule {

    val serviceModule = module {
        factory { YoutubeCastServiceManager(androidApplication()) }
        scope(named<YoutubeCastService>()) {
            scoped<YoutubeCastServiceContract.Controller> {
                YoutubeCastServiceController(getSource(), get(), get(), get(), get(), get())
            }
            scoped {
                PlayerControlsNotificationController(
                    view = get(),
                    context = androidApplication(),
                    log = get(),
                    state = get(),
                    toastWrapper = get(),
                    skipControl = get(),
                    mediaSessionManager = get()
                )
            }
            scoped<SkipContract.External> {
                SkipPresenter(
                    view = EmptySkipView(),
                    state = SkipContract.State(),
                    log = get(),
                    mapper = SkipContract.Mapper(timeSinceFormatter = get(), res = get()),
                    prefsWrapper = get(named<GeneralPreferences>())
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
                    log = get()
                )
            }
            scoped { PlayerControlsNotificationContract.State() }
        }
    }
}