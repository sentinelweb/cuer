package uk.co.sentinelweb.cuer.core.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.core.mappers.TimeStampMapper

object CoreJvmModule {
    val objectModule = module {
        factory { TimeStampMapper(log = get()) }
        factory { uk.co.sentinelweb.cuer.core.mappers.TimeFormatter() }
        factory { uk.co.sentinelweb.cuer.core.mappers.DateTimeFormatter() }
        factory { uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter(get()) }
    }
}