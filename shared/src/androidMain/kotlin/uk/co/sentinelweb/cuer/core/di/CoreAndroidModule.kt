package uk.co.sentinelweb.cuer.core.di

import org.koin.dsl.module

object CoreAndroidModule {
    val objectModule = module {
        factory { uk.co.sentinelweb.cuer.core.mappers.TimeStampMapper(log = get()) }
        factory { uk.co.sentinelweb.cuer.core.mappers.TimeFormatter() }
        factory { uk.co.sentinelweb.cuer.core.mappers.DateTimeFormatter() }
        factory { uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter(get()) }
    }
}