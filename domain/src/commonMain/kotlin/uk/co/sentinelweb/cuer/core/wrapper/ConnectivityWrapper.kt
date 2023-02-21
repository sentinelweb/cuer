package uk.co.sentinelweb.cuer.core.wrapper

interface ConnectivityWrapper {
    fun isConnected(): Boolean
    fun isMetered(): Boolean
    fun getWifiInfo(): WifiStateProvider.WifiState
    fun getWIFIIP(): String?
    fun getLocalIpAddress(): String?
    fun isNonMobileAvailable(): Boolean
    fun wifiIpAddress(): String?
}