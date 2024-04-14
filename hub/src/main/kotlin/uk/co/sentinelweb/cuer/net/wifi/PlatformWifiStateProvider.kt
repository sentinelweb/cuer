package uk.co.sentinelweb.cuer.net.wifi

import PlatformWifiInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider.WifiState
import java.io.IOException

class PlatformWifiStateProvider(
    private val log: LogWrapper
) : WifiStateProvider {

    private val _wifiStateFlow: MutableStateFlow<WifiState> = MutableStateFlow(WifiState())
    override val wifiStateFlow: StateFlow<WifiState> = _wifiStateFlow.asStateFlow()

    private val platformWifiInfo = PlatformWifiInfo()

    override var wifiState: WifiState
        get() = _wifiStateFlow.value
        private set(value) {
            _wifiStateFlow.value = value
        }

    override fun register() {
        // Start listening for WiFi state updates
        updateWifiInfo()
    }

    override fun unregister() {
        // No-op for this implementation
    }

    override fun updateWifiInfo() = runBlocking {
        try {
            log.d(wifiState.toString())
            val essid = platformWifiInfo.getEssid()
            val ipAddress = platformWifiInfo.getIpAddress()
            wifiState = WifiState(isConnected = essid != "Unknown", ssid = essid, ip = ipAddress)
            log.d(wifiState.toString())
        } catch (e: IOException) {
            // Handle error
            e.printStackTrace()
            wifiState = WifiState()
        }
    }
}
