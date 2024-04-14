package uk.co.sentinelweb.cuer.net

import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper

class IosConnectivityWrapper : ConnectivityWrapper {
    override fun isConnected(): Boolean = true

    override fun isMetered(): Boolean = false

//    override fun getWifiInfo(): WifiStateProvider.WifiState {
//        TODO("Not yet implemented")
//    }
//
//    override fun getWIFIIP(): String? {
//        TODO("Not yet implemented")
//    }
//
//    override fun getLocalIpAddress(): String? {
//        TODO("Not yet implemented")
//    }
//
//    override fun isNonMobileAvailable(): Boolean {
//        TODO("Not yet implemented")
//    }

    override fun wifiIpAddress(): String? {
        TODO("Not yet implemented")
    }
}