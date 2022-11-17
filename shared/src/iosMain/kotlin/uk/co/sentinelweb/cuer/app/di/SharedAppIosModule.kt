package uk.co.sentinelweb.cuer.app.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.repository.file.AssetOperations
import uk.co.sentinelweb.cuer.app.factory.OrchestratorFactory
import uk.co.sentinelweb.cuer.app.impl.ProxyFilter
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.net.IosConnectivityWrapper

object SharedAppIosModule {

    private val factoryModule = module {
        single { OrchestratorFactory() }
        single { ProxyFilter() }
    }

    private val utilModule = module {
        factory { AssetOperations() }
        factory<LogWrapper> { SystemLogWrapper() } // todo move to domain module?
    }

    private val netModule = module {
        factory<ConnectivityWrapper> { IosConnectivityWrapper() }
    }

    val modules = listOf(factoryModule)
        .plus(utilModule)
        .plus(netModule)

}