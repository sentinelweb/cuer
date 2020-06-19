package uk.co.sentinelweb.cuer.core.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.core.mappers.DateTimeMapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider

object CoreModule {
    val objectModule = module {
        factory { DateTimeMapper() }
        factory { CoroutineContextProvider() }
        factory { TimeProvider() }
    }
}