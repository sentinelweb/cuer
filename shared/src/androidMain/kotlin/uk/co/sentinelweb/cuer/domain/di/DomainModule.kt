package uk.co.sentinelweb.cuer.domain.di

import org.koin.dsl.module

object DomainModule {
    val objectModule = module {
        factory { uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator(get()) }
    }
}