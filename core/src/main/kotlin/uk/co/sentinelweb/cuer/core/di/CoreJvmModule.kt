package uk.co.sentinelweb.cuer.core.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.core.mappers.DateTimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeStampMapper

object CoreJvmModule {
    val objectModule = module {
        factory { TimeStampMapper(log = get()) }
        factory { TimeFormatter() }
        factory { DateTimeFormatter() }
    }
}