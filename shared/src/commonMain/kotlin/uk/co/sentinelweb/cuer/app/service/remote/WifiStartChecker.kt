package uk.co.sentinelweb.cuer.app.service.remote

import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.remote.server.LocalRepository

class WifiStartChecker(
    private val manager: RemoteServerContract.Manager,
    private val localRepository: LocalRepository,
) {
    fun checkToStartServer(wifiState: WifiStateProvider.WifiState) {
        val localNode = localRepository.localNode

        val shouldStartOnWifi =
            localNode.wifiAutoStart && (wifiState.isObscured || localNode.wifiAutoConnectSSIDs.let { it.isEmpty() || it.contains(wifiState.ssid) })

        if (manager.isRunning().not() && wifiState.isConnected && shouldStartOnWifi) {
            manager.start()
        }
    }
}