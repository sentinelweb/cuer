package uk.co.sentinelweb.cuer.app.service.remote

//import uk.co.sentinelweb.cuer.remote.server.RemoteServer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.remote.server.*
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage.MsgType

class RemoteServerServiceController constructor(
    private val notification: RemoteServerContract.Notification.External,
    private val webServer: RemoteWebServerContract,
    private val multi: MultiCastSocketContract,
    private val coroutines: CoroutineContextProvider,
//    private val connectivityWrapper: ConnectivityWrapper,
    private val log: LogWrapper,
    private val remoteRepo: RemotesRepository,
    private val localRepo: LocalRepository,
    private val wakeLockManager: WakeLockManager,
    private val wifiStateProvider: WifiStateProvider,
    private val service: RemoteServerContract.Service, //todo might need to just extract intf for stopSelf()
) : RemoteServerContract.Controller {

    init {
        log.tag(this)
    }

    private var _serverJob: Job? = null
    private var _multiJob: Job? = null
    private var _wifiJob: Job? = null

    override val isServerStarted: Boolean
        get() = webServer.isRunning.also { log.d("webServer.isRunning:${webServer.isRunning}") }

    private val address: Pair<String, Int>?
        get() = true
            .takeIf { webServer.isRunning }
            ?.let { wifiStateProvider.wifiState.ip }
            ?.let { it to webServer.port }
            ?.apply { log.d("address: $this ${webServer.isRunning}") }

    override val localNode: LocalNodeDomain
        get() = localRepo.localNode


    override fun initialise() {
        notification.updateNotification("Starting server...")
        _serverJob?.cancel()
        _serverJob = coroutines.ioScope.launch {
            webServer.start {
                coroutines.mainScope.launch {
                    address?.also { notification.updateNotification(it.http()) }
                }
            }
            log.d("webServer ended")
        }
        _multiJob = coroutines.ioScope.launch {
            multi.runSocketListener {
                coroutines.ioScope.launch {
                    delay(200)
                    multi.send(MsgType.Join)
                }
            }
            log.d("multicast ended")
        }
        _wifiJob = coroutines.mainScope.launch {
            wifiStateProvider.wifiStateFlow.collectLatest { state ->
                if (state.isConnected.not()) {
                    service.stopSelf()
                }
            }
        }
        wakeLockManager.acquireWakeLock()
    }

    override fun handleAction(action: String?) {
        notification.handleAction(action)
    }

    override suspend fun multicastPing() {
        multi.send(MsgType.Ping)
    }

    override fun destroy() {
        // fixme ?? cancel coroutines
        log.d("Controller destroy")
        wakeLockManager.releaseWakeLock()
        webServer.stop()
        _serverJob?.cancel()
        _serverJob = null
        notification.destroy()
        coroutines.ioScope.launch {
            multi.close()
            delay(50)
            _multiJob?.cancel()
            _multiJob = null
        }
        _wifiJob?.cancel()
        _wifiJob = null
        coroutines.mainScope.launch {
            remoteRepo.setUnAvailable()
        }
        log.d("Controller destroyed")
    }
}