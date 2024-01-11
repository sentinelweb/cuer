package uk.co.sentinelweb.cuer.app.service.remote

import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider

interface WifiStartCheckerContract {
    fun checkToStartServer(wifiState: WifiStateProvider.WifiState)
}