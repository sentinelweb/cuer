package uk.co.sentinelweb.cuer.app.service.remote

interface RemoteServerContract {

    interface Manager {
        fun start()
        fun stop()
        fun get(): Service?
        fun isRunning(): Boolean
    }

    interface Service {
        val isServerStarted: Boolean
        val address: String?
        fun stopSelf()

        companion object {
            fun instance(): Service? = null
        }
    }

    interface Controller {
        val isServerStarted: Boolean
        val address: String?
        fun initialise()
        fun handleAction(action: String?)
        fun destroy()
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
