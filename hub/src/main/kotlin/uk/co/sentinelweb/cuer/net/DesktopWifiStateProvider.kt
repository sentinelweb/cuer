package uk.co.sentinelweb.cuer.net

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider

class DesktopWifiStateProvider : WifiStateProvider {
    override val wifiStateFlow: Flow<WifiStateProvider.WifiState>
        get() = MutableSharedFlow()

    override fun register() {
        TODO("Not yet implemented")
    }

    override fun unregister() {
        TODO("Not yet implemented")
    }

    override fun updateWifiInfo() {
        TODO("Not yet implemented")
    }

}