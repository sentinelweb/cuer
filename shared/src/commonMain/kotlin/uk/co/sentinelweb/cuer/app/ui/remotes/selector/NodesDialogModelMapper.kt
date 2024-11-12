package uk.co.sentinelweb.cuer.app.ui.remotes.selector

import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.*
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.NodesDialogContract.Model.NodeModel
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.NodeDomain.DeviceType.OTHER
import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain

class NodesDialogModelMapper {
    fun mapNode(it: NodeDomain) = when (it) {
        is LocalNodeDomain -> mapNode(it)
        is RemoteNodeDomain -> mapNode(it)
        else -> throw IllegalArgumentException("Unknown node type: $it")
    }

    private fun mapNode(it: LocalNodeDomain): NodeModel =
        NodeModel(
            isLocal = true,
            id = it.id,
            title = "Local: ${it.hostname}",
            address = "${it.ipAddress}:${it.port}",
            device = it.device ?: "No device",
            deviceType = it.deviceType ?: OTHER,
            hostname = it.hostname ?: "No hostname",
            authType = it.authConfig::class.simpleName ?: "-",
            domain = it,
            screens = listOf(),
        )

    private fun mapNode(it: RemoteNodeDomain): NodeModel =
        NodeModel(
            isLocal = false,
            id = it.id,
            title = it.hostname ?: it.ipAddress,
            address = "${it.ipAddress}:${it.port}",
            device = it.device ?: "No device",
            deviceType = it.deviceType ?: OTHER,
            hostname = it.hostname ?: "No hostname",
            authType = it.authType::class.simpleName ?: "-",
            domain = it,
            screens = listOf(),
        )

    fun mapNodeAndScreen(node: RemoteNodeDomain, playerNodeDomain: PlayerNodeDomain): NodeModel =
        mapNode(node).copy(
            screens = playerNodeDomain.screens.map {
                Screen(
                    index = it.index,
                    width = it.width,
                    height = it.height,
                    refreshRate = it.refreshRate,
                    name = it.name,
                    domain = it,
                )
            }
        )
}
