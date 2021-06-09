package uk.co.sentinelweb.cuer.app.service.remote

class RemoteContract {

    interface Service {
        fun stopSelf()
    }

    interface Controller {
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