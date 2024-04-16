package uk.co.sentinelweb.cuer.app.receiver

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.service.remote.WifiStartChecker
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.tools.rule.MainCoroutineRule

class WifiStateReceiverTest {
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val connectivityWrapper = mockk<ConnectivityWrapper>(relaxed = true)
    private val log: LogWrapper = SystemLogWrapper()
    private val wifiStartChecker = mockk<WifiStartChecker>(relaxed = true)

    private lateinit var sut: WifiStateReceiver

    //    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
//    val wifiInfo: WifiInfo? = wifiManager.connectionInfo
//    return wifiInfo?.let {
//        it.ssid to wifiIpAddress()
    @Test
    fun onReceive() = runTest {
        val fixtSSID = "ssidValue"
        val mockwifiInfo = mockk<WifiInfo>(relaxed = true) {
            every { ssid } returns fixtSSID
            every { ipAddress } returns 1234
        }
        val mockWifiManager = mockk<WifiManager>(relaxed = true) {
            every { connectionInfo } returns mockwifiInfo
        }
        val mockContext = mockk<Context>(relaxed = true) {
            every { getSystemService(Context.WIFI_SERVICE) } returns mockWifiManager
        }
        val mockNetworkinfo = mockk<NetworkInfo>(relaxed = true) {
            every { type } returns ConnectivityManager.TYPE_WIFI
        }
        val mockIntent = mockk<Intent>(relaxed = true) {
            every { action } returns WifiManager.NETWORK_STATE_CHANGED_ACTION
            every { getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO) } returns mockNetworkinfo
        }

//        val wifiState = WifiStateProvider.WifiState(isConnected = false, ip = expectedIp)
//        val wifiStateConnected = WifiStateProvider.WifiState(isConnected = true, ip = expectedIp)
        //every { connectivityWrapper.getWifiInfo() }.returnsMany(wifiState, wifiStateConnected)
        //every { connectivityWrapper.wifiIpAddress() }.returns(expectedIp)

        startKoin { modules(module { factory { wifiStartChecker } }) }

        sut = WifiStateReceiver(mockContext, log)

        // fixme wifi test the full wifi state after fixing reciever
        sut.wifiStateFlow.test {
            awaitItem().apply {
                assertEquals(false, isConnected)
                assertNull(ip)
            }

            sut.onReceive(mockContext, mockIntent)

            awaitItem().apply {
                assertEquals(fixtSSID, ssid)
                assertEquals(true, isConnected)
                assertNotNull(ip)
            }
            cancelAndIgnoreRemainingEvents()
        }

    }
}