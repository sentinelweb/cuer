package uk.co.sentinelweb.cuer.core.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.core.mappers.DateFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider

object CoreModule {
    val objectModule = module {
        factory { TimeStampMapper(log = get()) }
        factory { CoroutineContextProvider() }
        factory { TimeProvider() }
        factory { TimeFormatter() }
        factory { DateFormatter() }
        factory { TimeSinceFormatter(get()) }
    }
}