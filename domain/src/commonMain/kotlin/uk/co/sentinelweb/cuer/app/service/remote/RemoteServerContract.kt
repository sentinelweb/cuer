package uk.co.sentinelweb.cuer.app.service.remote

import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage

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
        var stopListener: (() -> Unit)?
        fun stopSelf()
        suspend fun multicastPing()
    }

    interface Controller {
        val isServerStarted: Boolean
        val localNode: LocalNodeDomain
        fun initialise()
        fun handleAction(action: String?)
        fun destroy()
        suspend fun multicastPing()
    }

    interface AvailableMessageHandler {
        suspend fun messageReceived(msg: AvailableMessage)
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
