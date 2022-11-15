package uk.co.sentinelweb.cuer.app.di

import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.core.di.SharedCoreModule
import uk.co.sentinelweb.cuer.domain.BuildConfigDomain
import uk.co.sentinelweb.cuer.domain.di.SharedDomainModule
import uk.co.sentinelweb.cuer.net.di.SharedNetModule

private fun initKoinInternal(
    config: BuildConfigDomain,
    appDeclaration: KoinAppDeclaration = {}
) =
    startKoin {
        appDeclaration()
        val configModule = module {
            factory { config }
        }
        modules(
            listOf(SharedCoreModule.objectModule, SharedDomainModule.objectModule, SharedNetModule.objectModule)
                .plus(configModule)
                .plus(SharedAppModule.modules)
                .plus(IosAppModule.modules)
        )
    }

// called from iOS
@Suppress("unused")
fun initKoin(config: BuildConfigDomain) = initKoinInternal(config) { }