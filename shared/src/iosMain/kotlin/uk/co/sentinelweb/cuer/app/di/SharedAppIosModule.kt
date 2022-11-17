package uk.co.sentinelweb.cuer.app.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.repository.file.AssetOperations
import uk.co.sentinelweb.cuer.app.factory.OrchestratorFactory
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.net.ApiKeyProvider
import uk.co.sentinelweb.cuer.net.CuerYoutubeApiKeyProvider
import uk.co.sentinelweb.cuer.net.IosConnectivityWrapper
import uk.co.sentinelweb.cuer.net.client.ServiceType

object SharedAppIosModule {

    private val factoryModule = module {
        single { OrchestratorFactory() }
    }

    private val utilModule = module {
        factory { AssetOperations() }
        factory<LogWrapper> { SystemLogWrapper() }// todo move to domain module?
    }

    private val netModule = module {
        factory<ApiKeyProvider>(named(ServiceType.YOUTUBE)) { CuerYoutubeApiKeyProvider() }
        factory<ConnectivityWrapper> { IosConnectivityWrapper() }
    }

    val modules = listOf(factoryModule)
        .plus(utilModule)
        .plus(netModule)

}