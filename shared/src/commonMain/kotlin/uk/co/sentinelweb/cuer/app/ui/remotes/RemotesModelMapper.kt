package uk.co.sentinelweb.cuer.app.ui.remotes

import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.*
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.NodeDomain.DeviceType.OTHER
import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain

class RemotesModelMapper constructor(
    private val strings: StringDecoder,
    private val log: LogWrapper,
) {
    init {
        log.tag(this)
    }

    fun map(state: RemotesContract.MviStore.State): Model {
        return Model(
            title = state.localNode.hostname ?: "No title",
            imageUrl = "https://cuer-275020.firebaseapp.com/images/headers/remotes.png",
            localNode = state.localNode.let { mapLocalNode(it) },
            remoteNodes = state.remoteNodes.map { mapRemoteNode(it) },
            serverState = state.serverState,
            address = state.serverAddress,
            wifiState = state.wifiState,
        )
    }

    fun mapRemoteNode(it: RemoteNodeDomain): RemoteNodeModel =
        RemoteNodeModel(
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

    private fun mapLocalNode(it: LocalNodeDomain): LocalNodeModel =
        LocalNodeModel(
            id = it.id,
            title = it.hostname ?: it.ipAddress,
            address = "${it.ipAddress}:${it.port}",
            device = it.device ?: "No device",
            deviceType = it.deviceType ?: OTHER,
            hostname = it.hostname ?: "No hostname",
            authType = it.authConfig::class.simpleName ?: "-",
            domain = it
        )

    fun mapNodeAndScreen(node: RemoteNodeDomain, playerNodeDomain: PlayerNodeDomain): RemoteNodeModel =
        mapRemoteNode(node).copy(
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
