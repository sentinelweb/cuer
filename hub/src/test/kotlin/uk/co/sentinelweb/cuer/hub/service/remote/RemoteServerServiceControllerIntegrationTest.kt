package uk.co.sentinelweb.cuer.hub.service.remote

//import JnetPcapWifiStateProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext.startKoin
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.hub.di.test.RemoteServerControllerTestModules

class RemoteServerServiceControllerIntegrationTest : KoinComponent {



    private val sut: RemoteServerContract.Controller by inject()
    private val coroutines: CoroutineContextProvider by inject()
    private val wifiStateProvider: WifiStateProvider by inject()
    @Before
    fun setUp() {
        startKoin {
            modules(RemoteServerControllerTestModules.testModules)
        }
        wifiStateProvider.register()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun isServerStarted() = runBlocking {
        sut.initialise()
        delay(5000)
            while (sut.isServerStarted) {
                delay(2000)
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