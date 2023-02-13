package uk.co.sentinelweb.cuer.app.ui.local

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL_NETWORK
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.toGuidIdentifier
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.NodeModel
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.NodeDomain.DeviceType.OTHER

class LocalModelMapper constructor(
    private val strings: StringDecoder,
    private val log: LogWrapper,
) {
    init {
        log.tag(this)
    }

    fun map(state: LocalContract.MviStore.State): Model {
        return Model(
            title = "Configure Access",
            imageUrl = "https://cuer-275020.firebaseapp.com/images/headers/artificial-intelligence-3382507_640.jpg",
            localNode = state.localNode?.let { mapNode(it) } ?: dummyModel().localNode,
            serverState = state.serverState,
            address = state.serverAddress,
        ).also { log.d("mapped: $it") }
    }

    private fun mapNode(it: NodeDomain): NodeModel =
        NodeModel(
            id = it.id ?: "node-${it.ipAddress}-${it.port}".toGuidIdentifier(LOCAL_NETWORK),
            title = it.hostname ?: it.ipAddress,
            address = "${it.ipAddress}:${it.port}",
            device = it.device ?: "No device",
            deviceType = it.deviceType ?: OTHER,
            hostname = it.hostname ?: "No hostname"
        )

    private fun dummyModel() = Model(
        title = "Dummy",
        imageUrl = "https://cuer-275020.firebaseapp.com/images/headers/remotes.png",
        localNode = NodeModel(
            id = "".toGuidIdentifier(MEMORY),
            title = "DummyLocal",
            hostname = "DummyHostname",
            address = "d.d.d.d",
            device = "DummyDevice",
            deviceType = OTHER,
        ),
        address = "a.a.a.a:aaaa",
    )
}
