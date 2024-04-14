package uk.co.sentinelweb.cuer.core.wrapper

import kotlinx.coroutines.flow.Flow

interface WifiStateProvider {

    val wifiState: WifiState
    val wifiStateFlow: Flow<WifiState>

    data class WifiState(
        val isConnected: Boolean = false,
        val ssid: String? = null,
        val ip: String? = null,
//        val linkSpeed: Int? = null,
//        val bssid: String? = null,
//        val rssi: Int? = null,
        val isObscured: Boolean = false,
    ) {
        override fun toString(): String = "isConnected: $isConnected, ssid=\"$ssid\" ip=\"$ip\", isObscured=$isObscured"
    }

    fun register()
    fun unregister()
    fun updateWifiInfo()
}
