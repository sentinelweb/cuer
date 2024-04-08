package uk.co.sentinelweb.cuer.net

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.net.connectivity.ConnectivityMonitor

class DesktopWifiStateProvider(
    private val coroutines: CoroutineContextProvider,
    private val monitor: ConnectivityMonitor,
    private val connectivityWrapper: ConnectivityWrapper,
    private val log: LogWrapper
) : WifiStateProvider {

    private val _wifiStateFlow = MutableStateFlow(WifiStateProvider.WifiState())
    override val wifiStateFlow: Flow<WifiStateProvider.WifiState>
        get() = _wifiStateFlow

    init {
        log.tag(this)
        coroutines.mainScope.launch {
            monitor.connectivityStatus.collect { status ->
                this@DesktopWifiStateProvider._wifiStateFlow.value =
                    WifiStateProvider.WifiState(
                        isConnected = status,
                        ip = connectivityWrapper.wifiIpAddress()
                    )//.also { log.d(it.toString()) }

            }
        }
    }

    override fun register() = Unit

    override fun unregister() = Unit

    override fun updateWifiInfo() = Unit

}