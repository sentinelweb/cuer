package uk.co.sentinelweb.cuer.app.ui.remotes

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.toGuidIdentifier
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.NodeModel
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.NodeDomain.DeviceType.OTHER

class RemotesModelMapper constructor(
    private val strings: StringDecoder,
    private val log: LogWrapper,
) {
    init {
        log.tag(this)
    }

    fun map(state: RemotesContract.MviStore.State): Model {
        return Model(
            title = state.localNode?.hostname ?: state.localNode?.ipAddress ?: "No title",
            imageUrl = "https://cuer-275020.firebaseapp.com/images/headers/remotes..png",
            localNode = state.localNode?.let { mapNode(it) } ?: dummyModel().localNode,
            remoteNodes = state.remoteNodes.map { mapNode(it) },
            serverState = state.serverState,
            address = state.serverAddress,
        ).also { log.d("mapped: $it") }
    }

    private fun mapNode(it: NodeDomain): NodeModel =
        NodeModel(
            id = it.id,
            title = it.hostname ?: it.ipAddress,
            address = "http://${it.ipAddress}:${it.port}",
            device = it.device ?: "No device",
            deviceType = it.deviceType ?: OTHER,
            hostname = it.hostname ?: "No hostname"
        )

    private fun dummyModel() = Model(
        title = "Dummy",
        imageUrl = "https://cuer-275020.firebaseapp.com/images/headers/remotes..png",
        localNode = NodeModel(
            id = "".toGuidIdentifier(MEMORY),
            title = "DummyLocal",
            hostname = "DummyHostname",
            address = "d.d.d.d",
            device = "DummyDevice",
            deviceType = OTHER,
        ),
        remoteNodes = listOf(),
        address = "a.a.a.a:aaaa",
    )
}
