package uk.co.sentinelweb.cuer.app.service.remote

//import uk.co.sentinelweb.cuer.remote.server.RemoteServer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract.Controller.Companion.LOCAL_NODE_ID
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.remote.server.MultiCastSocketContract
import uk.co.sentinelweb.cuer.remote.server.MultiCastSocketContract.MulticastMessage.MsgType
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

    private val _remoteNodes: MutableStateFlow<List<NodeDomain>> = MutableStateFlow(listOf())
    override val remoteNodes: Flow<List<NodeDomain>>
        get() = _remoteNodes

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
                when (msg.type) {
                    MsgType.Join -> addNode(msg.node)
                    MsgType.Close -> removeNode(msg.node)
                    MsgType.Ping -> addNode(msg.node)
                    MsgType.PingReply -> addNode(msg.node)
                    MsgType.JoinReply -> addNode(msg.node)
                }
            }
            multi.startListener = {
                coroutines.ioScope.launch {
                    delay(50)
                    multi.send(MsgType.Join)
                }

            }
            multi.runSocketListener()
            log.d("multicast ended")
        }
    }

    fun addNode(node: NodeDomain) {
        if (node.ipAddress == connectivityWrapper.wifiIpAddress() && webServer.port == node.port) return
        val mutableList = _remoteNodes.value.toMutableList()
        val nodes = removeNodeInternal(node, mutableList)
        nodes.add(node)
        coroutines.mainScope.launch {
            _remoteNodes.emit(nodes)
        }
    }

    fun removeNode(node: NodeDomain) {
        val mutableList = _remoteNodes.value.toMutableList()
        val nodes = removeNodeInternal(node, mutableList)
        coroutines.mainScope.launch {
            _remoteNodes.emit(nodes)
        }
    }

    private fun removeNodeInternal(node: NodeDomain, nodes: MutableList<NodeDomain>): MutableList<NodeDomain> {
        nodes
            .find { it.ipAddress == node.ipAddress && it.port == node.port }
            ?.also { nodes.remove(it) }
        return nodes
    }

    override fun handleAction(action: String?) {
        notification.handleAction(action)
    }

    override fun ping() {
        multi.send(MsgType.Ping)
    }

    override fun destroy() {
        log.d("Controller destroy")
        coroutines.mainScope.launch {
            _remoteNodes.emit(listOf())
        }
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