package uk.co.sentinelweb.cuer.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.core.mappers.DateTimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProviderImpl
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator
import uk.co.sentinelweb.cuer.domain.mappers.PlaylistAndItemMapper
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator

object DomainModule {
    private val coreModule = module {
        factory { CoroutineContextProvider() }
        factory<TimeProvider> { TimeProviderImpl() }
        factory { TimeSinceFormatter(get(), get()) }
        factory { TimeFormatter() }
        factory { DateTimeFormatter() }
    }

    private val domainModule = module {
        factory { PlaylistItemCreator(get()) }
        factory { GuidCreator() }
        factory { PlaylistMutator() }
        factory { PlaylistAndItemMapper() }
    }

    val allModules = listOf(coreModule, domainModule)
}