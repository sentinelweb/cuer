package uk.co.sentinelweb.cuer.app.receiver

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.sentinelweb.cuer.app.service.remote.WifiStartChecker
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.tools.rule.MainCoroutineRule

class WifiStateReceiverTest {
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val connectivityWrapper = mockk<ConnectivityWrapper>(relaxed = true)
    private val log: LogWrapper = SystemLogWrapper()
    private val wifiStartChecker = mockk<WifiStartChecker>(relaxed = true)

    private lateinit var sut: WifiStateReceiver

    @Before
    fun setUp() {
    }

    @Test
    fun onReceive() = runTest {
        val mockContext = mockk<Context>(relaxed = true)
        val mockNetworkinfo = mockk<NetworkInfo>(relaxed = true) {
            every { type } returns ConnectivityManager.TYPE_WIFI
        }
        val mockIntent = mockk<Intent>(relaxed = true) {
            every { action } returns WifiManager.NETWORK_STATE_CHANGED_ACTION
            every { getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO) } returns mockNetworkinfo
        }
        val wifiState = WifiStateProvider.WifiState(isConnected = false)
        val wifiStateConnected = WifiStateProvider.WifiState(isConnected = true)
        every { connectivityWrapper.getWIFIInfo() }.returnsMany(wifiState, wifiStateConnected)
        sut = WifiStateReceiver(connectivityWrapper, log, wifiStartChecker)

        sut.wifiStateFlow.test {
            sut.onReceive(mockContext, mockIntent)
            assertEquals(wifiState, awaitItem())
            sut.onReceive(mockContext, mockIntent)
            assertEquals(wifiStateConnected, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}