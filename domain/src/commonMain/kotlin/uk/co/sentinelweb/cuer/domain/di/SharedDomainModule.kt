package uk.co.sentinelweb.cuer.domain.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator

object SharedDomainModule {
    val objectModule = module {
        factory { PlaylistItemCreator(get()) }
    }
}