package uk.co.sentinelweb.cuer.net.wifi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.net.connectivity.ConnectivityMonitor

class DesktopWifiStateProvider(
    private val coroutines: CoroutineContextProvider,
    private val monitor: ConnectivityMonitor,
    private val wifiStateProvider: WifiStateProvider,
    private val log: LogWrapper
) : WifiStateProvider {

    private val _wifiStateFlow = MutableStateFlow(WifiStateProvider.WifiState())
    override val wifiStateFlow: Flow<WifiStateProvider.WifiState>
        get() = _wifiStateFlow

    override val wifiState: WifiStateProvider.WifiState
        get() = _wifiStateFlow.value

    init {
        log.tag(this)
        coroutines.mainScope.launch {
            monitor.connectivityStatus.collectLatest { status ->
                this@DesktopWifiStateProvider._wifiStateFlow.value = wifiStateProvider.wifiState
            }
        }
    }

    override fun register() = Unit

    override fun unregister() = Unit

    override fun updateWifiInfo() = Unit

    //override fun wifiIpAddress(): String? = _wifiStateFlow.value.ip

}