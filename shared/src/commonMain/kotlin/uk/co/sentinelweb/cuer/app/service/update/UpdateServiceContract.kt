package uk.co.sentinelweb.cuer.app.service.update

import uk.co.sentinelweb.cuer.domain.MediaDomain

class UpdateServiceContract {

    interface Controller {
        fun initialise()
        fun update()
        fun destroy()
        fun handleAction(action: String?)
    }

    interface Service {
        fun stopSelf()
    }

    interface Notification {

        interface External {
            fun updateNotificationResult(mediaDomains: List<MediaDomain>)
            fun updateNotificationStatus(status: String)
            fun hideNotification()
            fun handleAction(action: String?)
            fun destroy()
        }

        interface Controller

        interface View {
            fun showNotification(summary: Model)
            fun stopSelf()
        }

        data class Model(
            val type: Type,
            val status: String?,
            val itemsUpdated: Int,
            val items: List<Item>
        ) {
            enum class Type { STATUS, RESULT }
            data class Item(
                val title: String
            )
        }
    }

    interface Manager {
        fun start()
        fun stop()
    }
}
