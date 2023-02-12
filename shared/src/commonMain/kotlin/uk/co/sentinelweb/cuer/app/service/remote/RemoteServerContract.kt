package uk.co.sentinelweb.cuer.app.service.remote

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.toGuidIdentifier
import uk.co.sentinelweb.cuer.domain.NodeDomain

interface RemoteServerContract {

    interface Manager {
        fun start()
        fun stop()
        fun getService(): Service?
        fun isRunning(): Boolean
    }

    interface Service {
        val isServerStarted: Boolean
        val address: String?
        val localNode: NodeDomain?
        val remoteNodes: Flow<List<NodeDomain>>
        fun stopSelf()
        fun ping()
    }

    interface Controller {
        val isServerStarted: Boolean
        val address: String?
        val localNode: NodeDomain?
        val remoteNodes: Flow<List<NodeDomain>>
        fun initialise()
        fun handleAction(action: String?)
        fun destroy()
        fun ping()

        companion object {
            val LOCAL_NODE_ID = "local-server-node".toGuidIdentifier(MEMORY)
        }
    }

    interface Notification {

        interface External {
            fun updateNotification(address: String)
            fun handleAction(action: String?)
            fun destroy()
        }

        interface Controller

        interface View {
            fun showNotification(address: String)
            fun stopSelf()
        }

        data class State constructor(
            val isStarted: Boolean = false
        )
    }
}
