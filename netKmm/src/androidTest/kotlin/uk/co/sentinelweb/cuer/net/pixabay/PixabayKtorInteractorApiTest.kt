package uk.co.sentinelweb.cuer.net.pixabay

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.net.ApiKeyProvider
import uk.co.sentinelweb.cuer.net.NetModuleConfig
import uk.co.sentinelweb.cuer.net.client.ErrorMapper
import uk.co.sentinelweb.cuer.net.client.KtorClientBuilder
import uk.co.sentinelweb.cuer.net.client.ServiceExecutor
import uk.co.sentinelweb.cuer.net.client.ServiceType
import uk.co.sentinelweb.cuer.net.pixabay.mapper.PixabayImageMapper


/**
 * This runs the real API for manual testing (keep test ignored)
 *
 * the system property only gets picked up when running from command line
 *
 * TO RUN FROM AS : set -DCUER_PIXABAY_API_KEY=api in configuration
 */
@Ignore("Real api test .. run manually only")
class PixabayKtorInteractorApiTest {

    private lateinit var service: PixabayService

    private var keyProvider = object : ApiKeyProvider {
        override val key: String = System.getProperty("CUER_PIXABAY_API_KEY")
            ?: throw IllegalArgumentException("could not get key")
    }

    private val log = SystemLogWrapper()
    private val config = NetModuleConfig(debug = true)
    private val connectivityWrapper = object : ConnectivityWrapper {
        override fun isConnected() = true
        override fun isMetered() = true
        override fun getWIFIID(): String? = "SSID"
        override fun getWIFIIP(): String? = "WIFI.IP"
        override fun getLocalIpAddress(): String? = ""
        override fun isNonMobileAvailable(): Boolean = true
        override fun wifiIpAddress(): String? = "WIFI.IP"
    }
    private lateinit var sut: PixabayInteractor

    @Before
    fun setUp() {
        val clientBuilder = KtorClientBuilder()
        val serviceExecutor = ServiceExecutor(clientBuilder.build(config, log), ServiceType.PIXABAY, log)
        service = PixabayService(serviceExecutor)

        sut = PixabayKtorInteractor(
            keyProvider = keyProvider,
            service = service,
            imageMapper = PixabayImageMapper(),
            errorMapper = ErrorMapper(SystemLogWrapper()),
            connectivity = connectivityWrapper,
            coContext = CoroutineContextProvider()
        )
    }

    @Test
    fun images() {
        runBlocking {
            val actual = sut.images(q = "trees")

            assertTrue(actual.isSuccessful)
            assertNotNull(actual.data)
        }
    }

}
