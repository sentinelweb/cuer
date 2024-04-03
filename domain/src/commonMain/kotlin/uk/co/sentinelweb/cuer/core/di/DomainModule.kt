package uk.co.sentinelweb.cuer.core.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.core.mappers.DateTimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProviderImpl

object DomainModule {
    val objectModule = module {
        factory { CoroutineContextProvider() }
        factory<TimeProvider> { TimeProviderImpl() }
        factory { TimeSinceFormatter(get(), get()) }
        factory { TimeFormatter() }
        factory { DateTimeFormatter() }
    }
}