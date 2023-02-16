package uk.co.sentinelweb.cuer.core.wrapper

import kotlinx.coroutines.flow.Flow

interface WifiStateProvider {

    val wifiStateFlow: Flow<WifiState>

    data class WifiState(
        val connected: Boolean = false,
        val ssid: String? = null,
        val ip: String? = null,
        val linkSpeed: Int? = null,
        val bssid: String? = null,
        val rssi: Int? = null,
    )

    fun register()
    fun unregister()
}
