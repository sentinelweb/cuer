package uk.co.sentinelweb.cuer.core.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.core.providers.PlayerConfigProvider
import uk.co.sentinelweb.cuer.core.providers.PlayerConfigProviderJvm

object JvmDomainModule {
    val objectModule = module {
        factory<PlayerConfigProvider> { PlayerConfigProviderJvm() }
    }
}