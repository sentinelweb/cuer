package uk.co.sentinelweb.cuer.app.di

import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.upcoming.UpcomingContract
import uk.co.sentinelweb.cuer.app.util.wrapper.PlatformLaunchWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ShareWrapper
import uk.co.sentinelweb.cuer.core.di.DomainModule
import uk.co.sentinelweb.cuer.db.di.DatabaseCommonModule
import uk.co.sentinelweb.cuer.db.di.DatabaseIosModule
import uk.co.sentinelweb.cuer.domain.BuildConfigDomain
import uk.co.sentinelweb.cuer.domain.di.SharedDomainModule
import uk.co.sentinelweb.cuer.net.ApiKeyProvider
import uk.co.sentinelweb.cuer.net.NetModuleConfig
import uk.co.sentinelweb.cuer.net.client.ServiceType
import uk.co.sentinelweb.cuer.net.di.DomainNetModule
import uk.co.sentinelweb.cuer.net.di.NetModule

private fun initKoinInternal(
    dependencies: SharedAppDependencies,
    appDeclaration: KoinAppDeclaration = {}
) =
    startKoin {
        appDeclaration()
        val configModule = module {
            factory { dependencies.config }
            factory {
                NetModuleConfig(debug = dependencies.config.isDebug)
            }
            factory<ApiKeyProvider>(named(ServiceType.YOUTUBE)) { dependencies.ytApiKey }
            factory<ApiKeyProvider>(named(ServiceType.PIXABAY)) { dependencies.pixabayApiKey }
            single { dependencies.platformLaunchWrapper }
            single { dependencies.shareWrapper }
            single<UpcomingContract.View> { dependencies.upcomingView }
        }


        modules(
            listOf(DomainModule.objectModule, SharedDomainModule.objectModule, DomainNetModule.objectModule)
                .plus(configModule)
                .plus(SharedAppModule.modules)
                .plus(SharedAppIosModule.modules)
                .plus(DatabaseCommonModule.modules)
                .plus(DatabaseIosModule.modules)
                .plus(NetModule.modules)
        )
    }

// called from iOS
@Suppress("unused")
fun initKoin(dependencies: SharedAppDependencies) = initKoinInternal(dependencies) { }

// these object are created in Swift and passed in at application start
data class SharedAppDependencies(
    val config: BuildConfigDomain,
    val ytApiKey: ApiKeyProvider,
    val pixabayApiKey: ApiKeyProvider,
    val shareWrapper: ShareWrapper,
    val platformLaunchWrapper: PlatformLaunchWrapper,
    // fixme not a great place for this - remove hack later
    val upcomingView: UpcomingContract.View,
)