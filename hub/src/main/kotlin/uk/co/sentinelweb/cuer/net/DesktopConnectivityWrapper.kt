package uk.co.sentinelweb.cuer.net

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.net.connectivity.ConnectivityMonitor
import java.net.InetAddress

class DesktopConnectivityWrapper(
    private val coroutines: CoroutineContextProvider,
    private val monitor: ConnectivityMonitor,
    private val log: LogWrapper
) : ConnectivityWrapper {

    private var status: Boolean = false

    init {
        log.tag(this)
        coroutines.mainScope.launch {
            monitor.connectivityStatus.collect { status ->
                this@DesktopConnectivityWrapper.status = status
                log.d("Connected: $status")
            }
        }
    }

    override fun isConnected(): Boolean = status

    override fun isMetered(): Boolean = false

    override fun getWifiInfo(): WifiStateProvider.WifiState = WifiStateProvider.WifiState(

    )

    //override fun getWIFIIP(): String? = "0.0.0.0"

//    override fun getLocalIpAddress(): String? = "0.0.0.0"

    override fun wifiIpAddress(): String? = try {
        InetAddress.getLocalHost().hostAddress
    } catch (ex: Exception) {
        ex.printStackTrace()
        null
    }
}