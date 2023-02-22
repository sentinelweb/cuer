package uk.co.sentinelweb.cuer.domain.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator
import uk.co.sentinelweb.cuer.domain.mappers.PlaylistAndItemMapper
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator

object SharedDomainModule {
    val objectModule = module {
        factory { PlaylistItemCreator(get()) }
        factory { GuidCreator() }
        factory { PlaylistMutator() }
        factory { PlaylistAndItemMapper() }
    }
}