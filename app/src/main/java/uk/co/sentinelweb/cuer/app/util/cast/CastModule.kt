package uk.co.sentinelweb.cuer.app.util.cast

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.util.cast.listener.ConnectionMonitor
import uk.co.sentinelweb.cuer.app.util.cast.listener.YoutubePlayerContextCreator

object CastModule {
    val castModule = module {
        factory { ChromeCastWrapper(androidApplication()) }
        factory {
            YoutubePlayerContextCreator(
                queue = get(),
                log = get(),
                mediaSessionManager = get(),
                castWrapper = get(),
                connectionMonitor = get()
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
    }
}