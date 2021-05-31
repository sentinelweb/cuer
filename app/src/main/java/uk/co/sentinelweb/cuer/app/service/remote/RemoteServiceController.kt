package uk.co.sentinelweb.cuer.app.service.remote

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.remote.server.RemoteServer

class RemoteServiceController constructor(
    private val service: RemoteContract.Service,
    private val notification: RemoteContract.Notification.External,
    private val webServer: RemoteServer,
    private val coroutines: CoroutineContextProvider
) : RemoteContract.Controller {

    //    init{
//        log.tag(this)
//    }
    private var serverJob: Job? = null

    override fun initialise() {
        notification.updateNotification("x")
        serverJob?.cancel()
        serverJob = coroutines.ioScope.launch {
            val address = webServer.fullAddress// todo get the proper address this is 127.0.0.1
            withContext(coroutines.Main) {
                notification.updateNotification(address)
            }
            webServer.start()
        }
    }

    override fun handleAction(action: String?) {
        notification.handleAction(action)
    }

    override fun destroy() {
        serverJob?.cancel()
        serverJob = null
        webServer.stop()
        notification.destroy()
    }

}