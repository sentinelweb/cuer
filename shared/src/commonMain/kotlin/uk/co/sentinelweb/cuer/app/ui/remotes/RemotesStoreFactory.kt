package uk.co.sentinelweb.cuer.app.ui.remotes

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore.*
import uk.co.sentinelweb.cuer.app.util.permission.LocationPermissionLaunch
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.remote.RemoteInteractor
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.ServerState
import uk.co.sentinelweb.cuer.remote.server.http
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage.MsgType.Ping

class RemotesStoreFactory constructor(
    private val storeFactory: StoreFactory = DefaultStoreFactory(),
    private val strings: StringDecoder,
    private val log: LogWrapper,
    private val prefs: MultiPlatformPreferencesWrapper,
    private val remoteServerManager: RemoteServerContract.Manager,
    private val coroutines: CoroutineContextProvider,
    private val localRepository: LocalRepository,
    private val remoteInteractor: RemoteInteractor,
    private val remotesRepository: RemotesRepository,
    private val locationPermissionLaunch: LocationPermissionLaunch,
    private val wifiStateProvider: WifiStateProvider,
) {

    init {
        log.tag(this)
    }

    private sealed class Result {
        data class SetNodes(val nodes: List<RemoteNodeDomain>) : Result()
        data class UpdateWifiState(val state: WifiStateProvider.WifiState) : Result()
        object UpdateServerState : Result()
    }

    private sealed class Action {
        object Init : Action()
    }

    private inner class ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(msg: Result): State =
            when (msg) {
                is Result.SetNodes -> copy(remoteNodes = msg.nodes)
                is Result.UpdateServerState -> copy(
                    serverState = if (remoteServerManager.isRunning()) ServerState.STARTED else ServerState.STOPPED,
                    serverAddress = remoteServerManager.getService()?.localNode?.http(),
                    localNode = localRepository.localNode, // remoteServerManager.getService()?.localNode ?:
                    wifiState = wifiStateProvider.wifiState
                ).also { log.d("it.serverAddress; ${it.serverAddress}") }

                is Result.UpdateWifiState -> copy(wifiState = msg.state)
            }
    }

    private class BootstrapperImpl() : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Action.Init)
        }
    }

    private inner class ExecutorImpl() : CoroutineExecutor<Intent, Action, State, Result, Label>() {

        override fun executeAction(action: Action, getState: () -> State) =
            when (action) {
                Action.Init -> init()
            }

        override fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                Intent.ActionSettings -> publish(Label.ActionSettings)
                Intent.ActionPasteAdd -> publish(Label.ActionPasteAdd)
                Intent.ActionSearch -> publish(Label.ActionSearch)
                Intent.ActionHelp -> publish(Label.ActionHelp)
                Intent.Up -> publish(Label.Up)
                Intent.ActionConfig -> config(intent)
                Intent.ActionPingMulticast -> pingMulticast(intent)
                Intent.ActionStartServer -> startServer(intent)
                Intent.ActionStopServer -> stopServer(intent)
                Intent.Refresh -> refresh()
                is Intent.ActionPingNode -> pingNode(intent)
                is Intent.WifiStateChange -> wifiStateChange(intent)
                is Intent.ActionObscuredPerm -> launchLocationPermission()
                is Intent.RemoteUpdate -> remotesUpdate(intent)
                is Intent.DeleteRemote -> deleteRemote(intent)
            }

        private fun deleteRemote(intent: Intent.DeleteRemote) {
            coroutines.mainScope.launch { remotesRepository.removeNode(intent.remote) }
        }

        private fun remotesUpdate(intent: Intent.RemoteUpdate) {
            dispatch(Result.SetNodes(intent.remotes))
        }

        private fun wifiStateChange(intent: Intent.WifiStateChange) {
            dispatch(Result.UpdateWifiState(intent.wifiState))
            log.d("got wifiStateChange: $intent")
            coroutines.mainScope.launch {
                delay(300)
                dispatch(Result.UpdateServerState)
            }
        }

        private fun launchLocationPermission() {
            locationPermissionLaunch.launchLocationPermission()
        }

        private fun pingNode(intent: Intent.ActionPingNode) {
            coroutines.ioScope.launch {
                remotesRepository.addUpdateNode(
                    intent.remote.copy(
                        isAvailable = remoteInteractor.available(Ping, intent.remote).isSuccessful
                    )
                )
            }
        }

        private fun config(intent: Intent) {
            publish(Label.ActionConfig)
        }

        private fun pingMulticast(intent: Intent) {
            if (remoteServerManager.isRunning()) {
                coroutines.ioScope.launch {
                    remoteServerManager.getService()?.multicastPing()
                    withContext(coroutines.Main) { dispatch(Result.UpdateServerState) } // is this necessary?
                }
            }
        }

        private var remotesJob: Job? = null
        private fun startServer(intent: Intent) {
            if (!remoteServerManager.isRunning()) {
                coroutines.mainScope.launch {
                    remoteServerManager.start()
                    // fixme limit?
                    while (remoteServerManager.getService()?.isServerStarted != true) {
                        print(".")
                        delay(50)
                    }
                    log.d("isRunning ${remoteServerManager.isRunning()} svc: ${remoteServerManager.getService()} address: ${remoteServerManager.getService()?.localNode?.http()}")

                    remoteServerManager.getService()?.stopListener = { dispatch(Result.UpdateServerState) }
                    dispatch(Result.UpdateServerState)
                }
            }
        }

        private fun stopServer(intent: Intent) {
            if (remoteServerManager.isRunning()) {
                coroutines.mainScope.launch {
                    remotesJob?.cancel()
                    remoteServerManager.stop()
                    delay(20)
                    dispatch(Result.UpdateServerState)
                }
            }
        }

        private fun init() {
            dispatch(Result.UpdateServerState)
            if (remoteServerManager.isRunning()) {
                remoteServerManager.getService()?.stopListener = { dispatch(Result.UpdateServerState) }
            }
        }

        private fun refresh() {
            dispatch(Result.UpdateServerState)
            wifiStateProvider.updateWifiInfo()
        }
    }

    fun create(): MviStore =
        object : MviStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RemotesStore",
            initialState = State(
                localNode = localRepository.localNode,
                wifiState = wifiStateProvider.wifiState
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl()
        ) {}


}