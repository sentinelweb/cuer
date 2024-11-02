package uk.co.sentinelweb.cuer.app.ui.remotes.selector

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.RemoteNodeModel
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Screen
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain

interface RemotesDialogContract {
    interface Launcher {
        fun launchRemotesDialog(
            callback: (NodeDomain, PlayerNodeDomain.Screen?) -> Unit,
            node: RemoteNodeDomain? = null,
            isSelectNodeOnly: Boolean = false,
        )

        fun hideRemotesDialog()
    }

    data class State(
        var selectedNode: NodeDomain? = null,
        var selectedNodeConfig: PlayerNodeDomain? = null,
        var isSelectNodeOnly: Boolean = false,
    )

    data class Model(
        val remotes: List<NodeModel>
    ) {
        data class NodeModel(
            val isLocal: Boolean,
            val id: Identifier<GUID>?,
            val title: String,
            val address: String,
            val hostname: String,
            val device: String,
            val deviceType: NodeDomain.DeviceType,
            val authType: String,
            val domain: NodeDomain,
            val screens: List<Screen>,
        )
        companion object {
            val blank = Model(listOf())
        }
    }
}
