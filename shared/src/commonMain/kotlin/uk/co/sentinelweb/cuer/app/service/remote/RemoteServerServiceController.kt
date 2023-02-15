package uk.co.sentinelweb.cuer.app.service.remote

//import uk.co.sentinelweb.cuer.remote.server.RemoteServer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.net.remote.RemoteInteractor
import uk.co.sentinelweb.cuer.remote.server.*
import uk.co.sentinelweb.cuer.remote.server.message.ConnectMessage
import uk.co.sentinelweb.cuer.remote.server.message.ConnectMessage.MsgType

class RemoteServerServiceController constructor(
    private val notification: RemoteServerContract.Notification.External,
    private val webServer: RemoteWebServerContract,
    private val multi: MultiCastSocketContract,
    private val coroutines: CoroutineContextProvider,
    private val connectivityWrapper: ConnectivityWrapper,
    private val log: LogWrapper,
    private val remoteRepo: RemotesRepository,
    private val localRepo: LocalRepository,
    private val connectMessageMapper: ConnectMessageMapper,
    private val remoteInteractor: RemoteInteractor,
    private val wakeLockManager: WakeLockManager
) : RemoteServerContract.Controller {

    init {
        log.tag(this)
    }

    private var _serverJob: Job? = null
    private var _multiJob: Job? = null

    override val isServerStarted: Boolean
        get() = webServer.isRunning

    private val address: Pair<String, Int>?
        get() = true
            .takeIf { webServer.isRunning }
            ?.let { connectivityWrapper.wifiIpAddress() }
            ?.let { it to webServer.port }
            ?.apply { log.d("address: $this ${webServer.isRunning}") }

    private var _localNode: LocalNodeDomain? = null
    override val localNode: LocalNodeDomain
        get() = (_localNode ?: throw IllegalStateException("local node not initialised"))
            .let { node -> address?.let { node.copy(ipAddress = it.first, port = it.second) } ?: node }

    private val connectMessageHandler = { msg: ConnectMessage ->
        val remote = mapRemoteNode(msg)
        log.d("receive connect: ${msg.type} remote: $remote")
        // todo decode remote
        when (msg.type) {
            MsgType.Join -> remoteRepo.addUpdateNode(remote)
            MsgType.Close -> remoteRepo.removeNode(remote)
            MsgType.Ping -> remoteRepo.addUpdateNode(remote)
            MsgType.PingReply -> remoteRepo.addUpdateNode(remote)
            MsgType.JoinReply -> remoteRepo.addUpdateNode(remote)
        }

        if (localRepo.getLocalNode().id != remote.id) {
            coroutines.mainScope.launch {
                when (msg.type) {
                    MsgType.Join -> remoteInteractor.connect(MsgType.JoinReply, remote)
                    MsgType.Ping -> remoteInteractor.connect(MsgType.PingReply, remote)

                    else -> Unit
                }
            }
        }
        Unit
    }

    private fun mapRemoteNode(msgDecoded: ConnectMessage) =
        connectMessageMapper.mapFromMulticastMessage(msgDecoded.node)


    override fun initialise() {
        coroutines.ioScope.launch {
            _localNode = localRepo.getLocalNode()
            //_remoteNodes.value = remoteRepo.loadAll()
        }
        notification.updateNotification("x.x.x.x")
        _serverJob?.cancel()
        _serverJob = coroutines.ioScope.launch {
            webServer.connectMessageListener = connectMessageHandler
            webServer.start {
                coroutines.mainScope.launch {
                    address?.also { notification.updateNotification(it.http()) }
                }
            }
            log.d("webServer ended")
        }
        _multiJob = coroutines.ioScope.launch {
            multi.connectMessageListener = connectMessageHandler
            multi.startListener = {
                coroutines.ioScope.launch {
                    delay(50)
                    multi.send(MsgType.Join)
                }
            }
            multi.runSocketListener()
            log.d("multicast ended")
        }
        wakeLockManager.acquireWakeLock()
    }

    override fun handleAction(action: String?) {
        notification.handleAction(action)
    }

    override fun ping() {
        multi.send(MsgType.Ping)
    }

    override fun destroy() {
        log.d("Controller destroy")
//        coroutines.mainScope.launch {
//            _remoteNodes.emit(listOf())
//        }
        wakeLockManager.releaseWakeLock()
        webServer.stop()
        _serverJob?.cancel()
        _serverJob = null
        notification.destroy()
        coroutines.ioScope.launch {
            multi.close()
            delay(50)
            _multiJob?.cancel()
        }
        log.d("Controller destroyed")
    }

}