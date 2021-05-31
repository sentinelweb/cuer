package uk.co.sentinelweb.cuer.app.service.remote

class RemoteNotificationController constructor(
    private val view: RemoteContract.Notification.View,
    private val state: RemoteContract.Notification.State
) : RemoteContract.Notification.External, RemoteContract.Notification.Controller {

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