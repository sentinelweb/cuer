package uk.co.sentinelweb.cuer.app.util.cast

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.cast.listener.ConnectionMonitor
import uk.co.sentinelweb.cuer.app.util.cast.listener.YoutubePlayerContextCreator

object CastModule {
    val castModule = module {
        single { ChromecastYouTubePlayerContextHolder(get(), get()) }
        single { CuerCastSessionListener(get(), get()) }
        factory { ChromeCastWrapper(androidApplication()) }
        factory { CuerSimpleVolumeController(get()) }
        factory {
            YoutubePlayerContextCreator(
                queue = get(),
                log = get(),
                mediaSessionManager = get(),
                castWrapper = get(),
                connectionMonitor = get(),
                timeProvider = get()
            )
        }
        factory {
            ConnectionMonitor(
                toast = get(),
                castWrapper = get(),
                coCxtProvider = get(),
                mediaSessionManager = get(),
                phoenixWrapper = get(),
                log = get()
            )
        }
        factory { CastDialogWrapper(get()) }
    }
}