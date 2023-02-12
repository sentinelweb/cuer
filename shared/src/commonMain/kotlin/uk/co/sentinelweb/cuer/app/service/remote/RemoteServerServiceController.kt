package uk.co.sentinelweb.cuer.app.service.remote

//import uk.co.sentinelweb.cuer.remote.server.RemoteServer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract.Controller.Companion.LOCAL_NODE_ID
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.NodeDomain
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
        get() = true.takeIf { webServer.isRunning }
            ?.let { connectivityWrapper.wifiIpAddress() }
            ?.let { webServer.fullAddress(it) }
            ?.apply { log.d("address: $this ${webServer.isRunning}") }
    override val localNode: NodeDomain?
        get() = webServer.let {
            NodeDomain(
                id = LOCAL_NODE_ID,
                ipAddress = connectivityWrapper.wifiIpAddress() ?: "-",
                port = webServer.port ?: -1,
                hostname = "tiger"
            )
        }

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
            log.d("webServer ended")
        }
        coroutines.ioScope.launch {
            multi.recieveListener = { msg ->
                log.d("multicast receive: $msg")
            }
            multi.startListener = {
                log.d("multicast started")
                coroutines.ioScope.launch {
                    delay(50)
                    multi.sendBroadcast()
                }

            }
            multi.runSocketListener()
            log.d("multicast ended")
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