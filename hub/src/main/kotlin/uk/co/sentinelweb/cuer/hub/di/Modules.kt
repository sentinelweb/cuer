package uk.co.sentinelweb.cuer.hub.di

import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.repository.file.AFile
import uk.co.sentinelweb.cuer.app.db.repository.file.JsonFileInteractor
import uk.co.sentinelweb.cuer.app.di.SharedAppModule
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerServiceController
import uk.co.sentinelweb.cuer.app.ui.common.resources.DefaultStringDecoder
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.util.permission.LocationPermissionLaunch
import uk.co.sentinelweb.cuer.core.di.DomainModule
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.domain.BuildConfigDomain
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.di.SharedDomainModule
import uk.co.sentinelweb.cuer.hub.service.remote.RemoteServerNotificationController
import uk.co.sentinelweb.cuer.hub.service.remote.RemoteServerService
import uk.co.sentinelweb.cuer.hub.ui.home.HomeUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.remotes.RemotesUiCoordinator
import uk.co.sentinelweb.cuer.hub.util.permission.EmptyLocationPermissionLaunch
import uk.co.sentinelweb.cuer.hub.util.remote.EmptyWakeLockManager
import uk.co.sentinelweb.cuer.hub.util.remote.RemoteServerServiceManager
import uk.co.sentinelweb.cuer.net.DesktopConnectivityWrapper
import uk.co.sentinelweb.cuer.net.DesktopWifiStateProvider
import uk.co.sentinelweb.cuer.net.NetModuleConfig
import uk.co.sentinelweb.cuer.net.connectivity.ConnectivityCheckManager
import uk.co.sentinelweb.cuer.net.connectivity.ConnectivityCheckTimer
import uk.co.sentinelweb.cuer.net.connectivity.ConnectivityChecker
import uk.co.sentinelweb.cuer.net.connectivity.ConnectivityMonitor
import uk.co.sentinelweb.cuer.net.di.NetModule
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.WakeLockManager
import uk.co.sentinelweb.cuer.remote.server.database.RemoteDatabaseAdapter
import uk.co.sentinelweb.cuer.remote.server.database.TestDatabase
import uk.co.sentinelweb.cuer.remote.server.di.RemoteModule
import java.io.File


object Modules {

    private val scopedModules = listOf(
        HomeUiCoordinator.uiModule,
        RemotesUiCoordinator.uiModule,
    )

    private val resourcesModule = module {
        factory<StringDecoder> { DefaultStringDecoder() }
    }

    private val utilModule = module {
        factory<LogWrapper> { SystemLogWrapper() }
        factory { LifecycleRegistry() }
        single<ConnectivityWrapper> { DesktopConnectivityWrapper(get(), get(), get()) }
        factory<LocationPermissionLaunch> { EmptyLocationPermissionLaunch() }
        factory<WifiStateProvider> { DesktopWifiStateProvider() }
    }

    private val connectivityModule = module {
        single { ConnectivityCheckManager(get(), get(), get()) }
        single { ConnectivityMonitor(get(), get(), get()) }
        single { ConnectivityCheckTimer() }
        single { ConnectivityChecker() }
    }

    private val configModule = module {
        single {
            // todo make SharedAppDependencies object : see ios di
            // todo fetch data from gradle props or OS
            BuildConfigDomain(
                true,
                true,
                1,
                "0.1-alpha",
                device = "Unknown",
                deviceType = NodeDomain.DeviceType.MAC
            )
        }
        factory {
            // todo use SharedAppDependencies object : see ios di
            NetModuleConfig(debug = true)
        }
    }

    private val remoteModule = module {
        // todo put in application files dir
        // todo make an initialiser object to create dir
        val filesDir = "/Users/robmunro/cuer_hub"
        File(filesDir).also {
            it.mkdirs()
            println(it.absolutePath)
        }
        single<RemoteServerContract.Manager> { RemoteServerServiceManager(log = get()) }
        single {
            "localNode.json"
                .let { AFile(File(filesDir, it).absolutePath) }
                .let { JsonFileInteractor(it, get()) }
                .let { LocalRepository(it, get(), get(), get()) }
        }
        single {
            "remoteNodes.json"
                .let { AFile(File(filesDir, it).absolutePath) }
                .let { JsonFileInteractor(it, get()) }
                .let { RemotesRepository(it, get(), get(), get()) }
        }
        // todo move to scope? - how to control? wrap inside RemoteServerServiceManager?
        factory<RemoteServerContract.Controller> {
            RemoteServerServiceController(
                notification = get(),
                webServer = get(),
                coroutines = get(),
                log = get(),
                connectivityWrapper = get(),
                multi = get(),
                localRepo = get(),
                remoteRepo = get(),
                wakeLockManager = get(),
                wifiStateProvider = get(),
                service = get(),
            )
        }
        factory<RemoteServerContract.Notification.External> {
            RemoteServerNotificationController(
//                view = get(),
//                state = get()
            )
        }
        factory<RemoteDatabaseAdapter> {
            TestDatabase.hardcoded() // todo make db
        }
        factory<WakeLockManager> { EmptyWakeLockManager() }
        factory<RemoteServerContract.Service> { RemoteServerService(get()) }
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
        .plus(scopedModules)
}