package uk.co.sentinelweb.cuer.core.wrapper

// fixme: why are there 2 wifi ip address fields
// fixme remove all unused fields
interface ConnectivityWrapper {
    fun isConnected(): Boolean
    fun isMetered(): Boolean
    fun getWifiInfo(): WifiStateProvider.WifiState

    //    fun getWIFIIP(): String?
//    fun getLocalIpAddress(): String?
    fun wifiIpAddress(): String?
}