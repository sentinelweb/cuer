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
import uk.co.sentinelweb.cuer.net.pixabay.mapper.PixabayImageMapper
import uk.co.sentinelweb.cuer.net.retrofit.ErrorMapper
import uk.co.sentinelweb.cuer.net.retrofit.RetrofitBuilder


/**
 * This runs the real API for manual testing (keep test ignored)
 *
 * the system property only gets picked up when running from command line
 *
 * TO RUN FROM AS : set -DCUER_PIXABAY_API_KEY=api in configuration
 */
class PixabayRetrofitInteractorApiTest {

    private lateinit var service: PixabayService

    private var keyProvider = object : ApiKeyProvider {
        override val key: String = System.getProperty("CUER_PIXABAY_API_KEY")
    }

    private val log = SystemLogWrapper()
    private val config = NetModuleConfig(debug = true)
    private val connectivityWrapper = object : ConnectivityWrapper {
        override fun isConnected() = true
        override fun isMetered() = true
    }
    private lateinit var sut: PixabayInteractor

    @Before
    fun setUp() {
        service = RetrofitBuilder(config, SystemLogWrapper()).let { it.buildPixabayService(it.buildPixabayClient()) }

        sut = PixabayRetrofitInteractor(
            keyProvider = keyProvider,
            service = service,
            imageMapper = PixabayImageMapper(),
            errorMapper = ErrorMapper(SystemLogWrapper()),
            connectivity = connectivityWrapper,
            coContext = CoroutineContextProvider()
        )
    }

    @Ignore("Real api test .. run manually only")
    @Test
    fun images() {
        runBlocking {
            val actual = sut.images(q = "trees")

            assertTrue(actual.isSuccessful)
            assertNotNull(actual.data)
        }
    }

}
