package uk.co.sentinelweb.cuer.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.core.providers.PlayerConfigProvider
import uk.co.sentinelweb.cuer.core.providers.PlayerConfigProviderJvm

object JvmDomainModule {
    private val coreModule = module {
        factory<PlayerConfigProvider> { PlayerConfigProviderJvm() }
    }

    val allModules = listOf(coreModule)
}