package uk.co.sentinelweb.cuer.app.di

import org.koin.dsl.module

object IosAppModule {

    val usecaseModule = module {

    }

    val utilModule = module {

    }

    val dbModule = module {

    }

    val modules = listOf(usecaseModule)
        .plus(utilModule)
        .plus(dbModule)
}