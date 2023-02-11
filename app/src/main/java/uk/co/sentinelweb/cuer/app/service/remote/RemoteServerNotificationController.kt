package uk.co.sentinelweb.cuer.app.service.remote

class RemoteServerNotificationController constructor(
    private val view: RemoteServerContract.Notification.View,
    private val state: RemoteServerContract.Notification.State
) : RemoteServerContract.Notification.External, RemoteServerContract.Notification.Controller {
    //    init{
//        log.tag(this)
//    }
    override fun updateNotification(address: String) {
        view.showNotification(address)
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