package uk.co.sentinelweb.cuer.app.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.factory.OrchestratorFactory

object IosAppModule {

    val factoryModule = module {
        single { OrchestratorFactory() }
    }

    val utilModule = module {

    }

    val dbModule = module {

    }

    val modules = listOf(factoryModule)
        .plus(utilModule)
        .plus(dbModule)
}