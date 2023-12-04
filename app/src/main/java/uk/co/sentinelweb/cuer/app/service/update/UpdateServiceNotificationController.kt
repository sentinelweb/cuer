package uk.co.sentinelweb.cuer.app.service.update

import uk.co.sentinelweb.cuer.app.service.update.UpdateServiceContract.Notification.Model
import uk.co.sentinelweb.cuer.domain.MediaDomain

class UpdateServiceNotificationController constructor(
    private val view: UpdateServiceContract.Notification.View,
) : UpdateServiceContract.Notification.External, UpdateServiceContract.Notification.Controller {
    //    init{
//        log.tag(this)
//    }
    override fun updateNotification(mediaDomains: List<MediaDomain>) {
        view.showNotification(mediaDomains
            .let {
                Model(
                    type = Model.Type.RESULT,
                    status = null,
                    itemsUpdated = it.size,
                    items = it.map { Model.Item(title = it.title ?: "No title") })
            }
        )
    }

    override fun updateNotification(status: String) {
        view.showNotification(Model(type = Model.Type.STATUS, status = status, itemsUpdated = -1, items = emptyList()))
    }

    override fun hideNotification() {
        view.stopSelf()
    }

    override fun handleAction(action: String?) {
        when (action) {
            ACTION_STOP -> view.stopSelf()
        }
    }

    override fun destroy() {
    }

    companion object {
        const val ACTION_STOP = "stop"
    }
}