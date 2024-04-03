package uk.co.sentinelweb.cuer.net

import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider

// todo get these from jvm - not possible hav to poll internet connection
class DesktopConnectivityWrapper : ConnectivityWrapper {
    override fun isConnected(): Boolean = true

    override fun isMetered(): Boolean = false

    override fun getWifiInfo(): WifiStateProvider.WifiState = WifiStateProvider.WifiState(

    )

    override fun getWIFIIP(): String? = "0.0.0.0"

    override fun getLocalIpAddress(): String? = "0.0.0.0"

    override fun isNonMobileAvailable(): Boolean = true

    override fun wifiIpAddress(): String? = "0.0.0.0"
}