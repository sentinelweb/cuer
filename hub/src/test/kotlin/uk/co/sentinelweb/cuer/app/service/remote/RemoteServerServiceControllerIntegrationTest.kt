package uk.co.sentinelweb.cuer.app.service.remote

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.repository.file.AFile
import uk.co.sentinelweb.cuer.app.db.repository.file.JsonFileInteractor
import uk.co.sentinelweb.cuer.app.di.SharedAppModule
import uk.co.sentinelweb.cuer.core.di.DomainModule
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.domain.BuildConfigDomain
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.di.SharedDomainModule
import uk.co.sentinelweb.cuer.hub.util.remote.EmptyWakeLockManager
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

class RemoteServerServiceControllerIntegrationTest : KoinComponent {


    private val remoteModule = module {
        val localResourcePath = "uk/co/sentinelweb/cuer/app/service/remote/localNode_no_address.json"
        val url = (Thread.currentThread().contextClassLoader.getResource(localResourcePath)
            ?: throw IllegalArgumentException("Resource not found: $localResourcePath"))
        println(url)
        val base = "/Users/robmunro/repos/personal/cuer/hub/src/test/resources/"
        single {
            "localNode.json"
                .let { AFile(base + localResourcePath) }
                .let { JsonFileInteractor(it, get()) }
                .let { LocalRepository(it, get(), get(), get()) }
        }
        single {
            "remoteNodes.json"
                .let { AFile(File(url.toExternalForm(), it).absolutePath) }
                .let { JsonFileInteractor(it, get()) }
                .let { RemotesRepository(it, get(), get(), get()) }
        }
        factory<WakeLockManager> { EmptyWakeLockManager() }
    }

    val serviceModule = module {
        //factory<RemoteServerContract.Service> { RemoteServerService(get()) }
        //single<RemoteServerContract.Manager> { RemoteServerServiceManager(get()) }
        //scope(named<RemoteServerService>()) {
        single<RemoteServerContract.Controller> {
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
        single<RemoteServerContract.Notification.External> { TestNotifExternal() }

        single<RemoteServerContract.Service> { TestService() }
        single { RemoteServerContract.Notification.State() }
        //}

        factory<RemoteDatabaseAdapter> {
            TestDatabase.hardcoded()
        }
    }

    private val utilModule = module {
        factory<LogWrapper> { SystemLogWrapper() }
    }

    private val connectivityModule = module {
        single<ConnectivityWrapper> { DesktopConnectivityWrapper(get(), get(), get(), get()) }
        single<WifiStateProvider> { DesktopWifiStateProvider(get(), get(), get(), get()) }
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
                "0.1-test",
                device = "Test",
                deviceType = NodeDomain.DeviceType.MAC
            )
        }
        factory {
            // todo use SharedAppDependencies object : see ios di
            NetModuleConfig(debug = true)
        }
    }

    private class TestNotifExternal : RemoteServerContract.Notification.External {
        override fun updateNotification(address: String) = Unit

        override fun handleAction(action: String?) = Unit

        override fun destroy() = Unit
    }

    private class TestService : RemoteServerContract.Service {
        override val isServerStarted: Boolean
            get() = TODO("Not yet implemented")
        override val localNode: LocalNodeDomain
            get() = TODO("Not yet implemented")
        override var stopListener: (() -> Unit)?
            get() = TODO("Not yet implemented")
            set(value) {}

        override fun stopSelf() {
            println("stopSelf() called")
            Exception().printStackTrace()
        }

        override suspend fun multicastPing() {
            TODO("Not yet implemented")
        }
    }

    val testModules = listOf(
        connectivityModule,
        remoteModule,
        serviceModule,
        utilModule,
        configModule,
        DomainModule.objectModule,
        RemoteModule.objectModule,
        SharedDomainModule.objectModule,
    )
        .plus(SharedAppModule.modules)
        .plus(NetModule.modules)

    private val sut: RemoteServerContract.Controller by inject()
    private val coroutines: CoroutineContextProvider by inject()

    @Before
    fun setUp() {
        startKoin {
            modules(testModules)
        }
    }

    @After
    fun tearDown() {
    }

    @Test
    fun isServerStarted() = runTest {
        sut.initialise()
        coroutines.defaultScope.launch {
            while (sut.isServerStarted) {
                delay(2000)
            }
        }
    }

    @Test
    fun getLocalNode() {
    }

    @Test
    fun initialise() {
    }

    @Test
    fun handleAction() {
    }

    @Test
    fun multicastPing() {
    }

    @Test
    fun destroy() {
    }
}