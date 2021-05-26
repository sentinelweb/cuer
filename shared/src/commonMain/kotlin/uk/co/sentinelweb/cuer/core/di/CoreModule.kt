package uk.co.sentinelweb.cuer.core.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter

import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider

object CoreModule {
    val objectModule = module {
        factory { CoroutineContextProvider() }
        factory { TimeProvider() }
        factory { TimeSinceFormatter(get()) }
    }
}