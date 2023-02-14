package uk.co.sentinelweb.cuer.app.service.remote

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain

interface RemoteServerContract {

    interface Manager {
        fun start()
        fun stop()
        fun getService(): Service?
        fun isRunning(): Boolean
    }

    interface Service {
        val isServerStarted: Boolean
        val localNode: LocalNodeDomain
        val remoteNodes: Flow<List<RemoteNodeDomain>>
        fun stopSelf()
        fun ping()
    }

    interface Controller {
        val isServerStarted: Boolean
        val localNode: LocalNodeDomain
        val remoteNodes: Flow<List<RemoteNodeDomain>>
        fun initialise()
        fun handleAction(action: String?)
        fun destroy()
        fun ping()
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
