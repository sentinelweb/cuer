package uk.co.sentinelweb.cuer.app.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.repository.file.AssetOperation
import uk.co.sentinelweb.cuer.app.factory.OrchestratorFactory

object SharedAppIosModule {

    val factoryModule = module {
        single { OrchestratorFactory() }
    }

    val utilModule = module {
        factory { AssetOperation() }
    }

    val dbModule = module {

    }

    val modules = listOf(factoryModule)
        .plus(utilModule)
        .plus(dbModule)
}