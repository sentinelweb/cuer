package uk.co.sentinelweb.cuer.app.service.remote

//import uk.co.sentinelweb.cuer.remote.server.RemoteServer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract

class RemoteServerServiceController constructor(
    private val notification: RemoteServerContract.Notification.External,
    private val webServer: RemoteWebServerContract,
    private val multi: MultiCastSocketContract,
    private val coroutines: CoroutineContextProvider,
    private val connectivityWrapper: ConnectivityWrapper,
    private val log: LogWrapper
) : RemoteServerContract.Controller {

    init {
        log.tag(this)
    }

    private var serverJob: Job? = null

    override val isServerStarted: Boolean
        get() = webServer.isRunning

    override val address: String?
        get() = let {
            if (webServer.isRunning) connectivityWrapper.wifiIpAddress()?.let { webServer.fullAddress(it) }
            else null
        }.apply { log.d("address: $this ${webServer.isRunning}") }

    override fun initialise() {
        notification.updateNotification("x")
        serverJob?.cancel()
        serverJob = coroutines.ioScope.launch {
            withContext(coroutines.Main) {
                connectivityWrapper.wifiIpAddress()
                    ?.also { webServer.fullAddress(it) }
                    ?.also { notification.updateNotification(it) }
            }
            webServer.start()
        }
        coroutines.ioScope.launch {
            multi.recieveListener = { msg ->
                log.d("multicast recieve: $msg")
            }
            multi.startListener = {
                log.d("multicast started")
                coroutines.ioScope.launch {
                    delay(50)
                    multi.sendBroadcast()
                }

            }
            multi.runSocketListener()
        }
    }

    override fun handleAction(action: String?) {
        notification.handleAction(action)
    }

    override fun destroy() {
        log.d("Controller destroy")
        serverJob?.cancel()
        serverJob = null
        webServer.stop()
        notification.destroy()
        coroutines.ioScope.launch {
            delay(50)
            multi.close()
        }

        log.d("Controller destroyed")
    }

}