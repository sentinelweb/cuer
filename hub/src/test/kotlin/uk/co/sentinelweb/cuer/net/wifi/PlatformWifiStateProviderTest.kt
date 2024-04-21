package uk.co.sentinelweb.cuer.net.wifi

import PlatformWifiInfo
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.service.remote.WifiStartChecker
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

@ExperimentalCoroutinesApi
class PlatformWifiStateProviderTest {

    private val wifiStateChecker: WifiStartChecker = mockk(relaxed = true)
    private val platformWifiInfo: PlatformWifiInfo = mockk(relaxed = true)
    private val log: LogWrapper = mockk(relaxed = true)

    private lateinit var provider: PlatformWifiStateProvider

    @Before
    fun setUp() {
        provider = PlatformWifiStateProvider(
            wifiStartChecker = wifiStateChecker,
            platformWifiInfo = platformWifiInfo,
            log = log,
        )
    }

    @Test
    fun register_shouldEmitWifiState() = runTest {
        val fixtSsid = "test_ssid"
        every { platformWifiInfo.getEssid() } returns fixtSsid
        val fixtIp = "test_ip"
        every { platformWifiInfo.getIpAddress() } returns fixtIp

        provider.register()

        verify { platformWifiInfo.getEssid() }
        verify { platformWifiInfo.getIpAddress() }
        provider.wifiStateFlow.test {
            val wifiState = awaitItem()
            assertNotNull(wifiState)
            assertEquals(wifiState.ip, fixtIp)
            assertEquals(wifiState.ssid, fixtSsid)
            expectNoEvents()
            verify { wifiStateChecker.checkToStartServer(wifiState) }
        }
    }
}