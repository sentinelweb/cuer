package uk.co.sentinelweb.cuer.app.service.remote

import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.remote.server.LocalRepositoryContract

class WifiStartChecker(
    private val manager: RemoteServerContract.Manager,
    private val localRepository: LocalRepositoryContract,
) : WifiStartCheckerContract {
    override fun checkToStartServer(wifiState: WifiStateProvider.WifiState) {
        val localNode = localRepository.getLocalNode()

        val shouldStartOnWifi =
            localNode.wifiAutoStart && (wifiState.isObscured || localNode.wifiAutoConnectSSIDs.let { it.isEmpty() || it.contains(wifiState.ssid) })

        if (manager.isRunning().not() && wifiState.isConnected && shouldStartOnWifi) {
            manager.start()
        }
    }
}