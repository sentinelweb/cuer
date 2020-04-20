package uk.co.sentinelweb.cuer.app.service.cast

import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module

object YoutubeCastServiceModule {

    val serviceModule = module {
        factory { YoutubeCastServiceManager(androidApplication()) }
        scope(named<YoutubeCastService>()) {
            scoped { YoutubeCastServiceController(getSource(), get(), get()) }
        }
    }
}