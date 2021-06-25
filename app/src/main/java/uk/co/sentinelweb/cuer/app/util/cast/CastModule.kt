package uk.co.sentinelweb.cuer.app.util.cast

import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.cast.listener.YoutubePlayerContextCreator
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences

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
                timeProvider = get(),
                prefs = get()
            )
        }
        factory { CastDialogWrapper(get()) }
    }
}