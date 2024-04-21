package uk.co.sentinelweb.cuer.hub.di

import PlatformWifiInfo
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.repository.file.AFile
import uk.co.sentinelweb.cuer.app.db.repository.file.AssetOperations
import uk.co.sentinelweb.cuer.app.db.repository.file.JsonFileInteractor
import uk.co.sentinelweb.cuer.app.di.SharedAppModule
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.app.ui.common.resources.DefaultStringDecoder
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.util.permission.LocationPermissionLaunch
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.core.di.DomainModule
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.db.di.DatabaseCommonModule
import uk.co.sentinelweb.cuer.db.di.JvmDatabaseModule
import uk.co.sentinelweb.cuer.domain.BuildConfigDomain
import uk.co.sentinelweb.cuer.domain.di.SharedDomainModule
import uk.co.sentinelweb.cuer.hub.BuildConfigInject
import uk.co.sentinelweb.cuer.hub.service.remote.RemoteServerService
import uk.co.sentinelweb.cuer.hub.service.remote.RemoteServerServiceManager
import uk.co.sentinelweb.cuer.hub.service.update.UpdateService
import uk.co.sentinelweb.cuer.hub.ui.home.HomeUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.local.LocalUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.remotes.RemotesUiCoordinator
import uk.co.sentinelweb.cuer.hub.util.permission.EmptyLocationPermissionLaunch
import uk.co.sentinelweb.cuer.hub.util.platform.getNodeDeviceType
import uk.co.sentinelweb.cuer.hub.util.platform.getOSData
import uk.co.sentinelweb.cuer.hub.util.remote.EmptyWakeLockManager
import uk.co.sentinelweb.cuer.hub.util.share.scan.TodoLinkScanner
import uk.co.sentinelweb.cuer.net.ApiKeyProvider
import uk.co.sentinelweb.cuer.net.NetModuleConfig
import uk.co.sentinelweb.cuer.net.client.ServiceType
import uk.co.sentinelweb.cuer.net.connectivity.ConnectivityCheckManager
import uk.co.sentinelweb.cuer.net.connectivity.ConnectivityCheckTimer
import uk.co.sentinelweb.cuer.net.connectivity.ConnectivityChecker
import uk.co.sentinelweb.cuer.net.connectivity.ConnectivityMonitor
import uk.co.sentinelweb.cuer.net.di.NetModule
import uk.co.sentinelweb.cuer.net.key.CuerPixabayApiKeyProvider
import uk.co.sentinelweb.cuer.net.key.CuerYoutubeApiKeyProvider
import uk.co.sentinelweb.cuer.net.wifi.DesktopConnectivityWrapper
import uk.co.sentinelweb.cuer.net.wifi.PlatformWifiStateProvider
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.WakeLockManager
import uk.co.sentinelweb.cuer.remote.server.di.RemoteModule
import java.io.File


object Modules {

    private val scopedModules = listOf(
        HomeUiCoordinator.uiModule,
        RemotesUiCoordinator.uiModule,
        RemoteServerService.serviceModule,
        UpdateService.serviceModule,
        LocalUiCoordinator.uiModule,
    )

    private val resourcesModule = module {
        factory<StringDecoder> { DefaultStringDecoder() }
        factory { AssetOperations() }
    }

    private val utilModule = module {
        factory<LogWrapper> { SystemLogWrapper() }
        factory { LifecycleRegistry() }
        factory<LocationPermissionLaunch> { EmptyLocationPermissionLaunch() }
    }

    private val connectivityModule = module {
        single<ConnectivityWrapper> { DesktopConnectivityWrapper(get(), get(), get(), get()) }
//        single<WifiStateProvider> { DesktopWifiStateProvider(get(), get(), get(), get()) }
        single<WifiStateProvider> { PlatformWifiStateProvider(get(),get(), get()) }
        single { PlatformWifiInfo() }
        single { ConnectivityCheckManager(get(), get(), get()) }
        single { ConnectivityMonitor(get(), get(), get()) }
        single { ConnectivityCheckTimer() }
        single { ConnectivityChecker() }
    }

    private val configModule = module {
        single {
            // todo make SharedAppDependencies object : see ios di
            BuildConfigDomain(
                isDebug = BuildConfigInject.isDebug,
                cuerRemoteEnabled = BuildConfigInject.cuerRemoteEnabled,
                versionCode = BuildConfigInject.versionCode,
                version = BuildConfigInject.version,
                device = getOSData(),
                deviceType = getNodeDeviceType()
            )
        }
        factory {
            // todo use SharedAppDependencies object : see ios di
            NetModuleConfig(debug = true)
        }
        factory<ApiKeyProvider>(named(ServiceType.YOUTUBE)) { CuerYoutubeApiKeyProvider() }
        factory<ApiKeyProvider>(named(ServiceType.PIXABAY)) { CuerPixabayApiKeyProvider() }
    }

    private val remoteModule = module {
        // todo put in application files dir
        // todo make an initialiser object to create dir
        val filesDir = "/Users/robmunro/cuer_hub"
        File(filesDir).also {
            it.mkdirs()
            println(it.absolutePath)
        }
        single {
            "localNode.json"
                .let { AFile(File(filesDir, it).absolutePath) }
                .let { JsonFileInteractor(it, get()) }
                .let { LocalRepository(it, get(), get(), get(), get()) }
        }
        single {
            "remoteNodes.json"
                .let { AFile(File(filesDir, it).absolutePath) }
                .let { JsonFileInteractor(it, get()) }
                .let { RemotesRepository(it, get(), get(), get()) }
        }
        factory<WakeLockManager> { EmptyWakeLockManager() }
        factory<LinkScanner> { TodoLinkScanner() }
        factory<RemoteServerContract.Service> { RemoteServerService(get()) }
        single<RemoteServerContract.Manager> { RemoteServerServiceManager(get()) }

    }

    val allModules = listOf(
        resourcesModule,
        utilModule,
        remoteModule,
        configModule,
        connectivityModule,
        DomainModule.objectModule,
        RemoteModule.objectModule,
        SharedDomainModule.objectModule,
    )
        .plus(SharedAppModule.modules)
        .plus(NetModule.modules)
        .plus(JvmDatabaseModule.modules)
        .plus(DatabaseCommonModule.modules)
        .plus(scopedModules)
}