package uk.co.sentinelweb.cuer.app.service.remote

import uk.co.sentinelweb.cuer.domain.LocalNodeDomain

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
        fun multicastPing()
    }

    interface Controller {
        val isServerStarted: Boolean
        val localNode: LocalNodeDomain
        fun initialise()
        fun handleAction(action: String?)
        fun destroy()
        fun multicastPing()
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
