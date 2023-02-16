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
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.remote.RemoteInteractor
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.ServerState
import uk.co.sentinelweb.cuer.remote.server.http
import uk.co.sentinelweb.cuer.remote.server.message.ConnectMessage.MsgType.Ping

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
    private val connectivityWrapper: ConnectivityWrapper,
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
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.SetNodes -> copy(remoteNodes = result.nodes)
                is Result.UpdateServerState -> copy(
                    serverState = if (remoteServerManager.isRunning()) ServerState.STARTED else ServerState.STOPPED,
                    serverAddress = remoteServerManager.getService()?.localNode?.http(),
                    localNode = remoteServerManager.getService()?.localNode ?: localRepository.getLocalNode()
                )

                is Result.UpdateWifiState -> copy(wifiState = result.state)
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
                Action.Init -> testLoad()
            }

        override fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                Intent.ActionSettings -> publish(Label.ActionSettings)
                Intent.ActionPasteAdd -> publish(Label.ActionPasteAdd)
                Intent.ActionSearch -> publish(Label.ActionSearch)
                Intent.ActionHelp -> publish(Label.ActionHelp)
                Intent.Up -> publish(Label.Up)
                Intent.ActionConfig -> config(intent, getState())
                Intent.ActionPingMulticast -> pingMulticast(intent, getState())
                Intent.ActionStartServer -> startServer(intent, getState())
                Intent.ActionStopServer -> stopServer(intent, getState())
                Intent.Refresh -> dispatch(Result.UpdateServerState)
                is Intent.ActionPingNode -> pingNode(intent, getState())
                is Intent.WifiStateChange -> dispatch(Result.UpdateWifiState(intent.wifiState))
            }

        private fun pingNode(intent: Intent.ActionPingNode, state: State) {
            coroutines.ioScope.launch {
                remoteInteractor.connect(Ping, intent.remote)
            }
        }

        private fun config(intent: Intent, state: State) {
            publish(Label.ActionConfig)
        }

        private fun pingMulticast(intent: Intent, state: State) {
            if (remoteServerManager.isRunning()) {
                coroutines.ioScope.launch {
                    remoteServerManager.getService()?.ping()
                    withContext(coroutines.Main) { dispatch(Result.UpdateServerState) }
                }
            }
        }

        private var remotesJob: Job? = null
        private fun stopServer(intent: Intent, state: State) {
            if (remoteServerManager.isRunning()) {
                coroutines.mainScope.launch {
                    remotesJob?.cancel()
                    remoteServerManager.stop()
                    delay(20)
                    dispatch(Result.UpdateServerState)
                    remotesRepository.updatesCallback = null
                    dispatch(Result.SetNodes(listOf()))
                }
            }
        }

        private fun startServer(intent: Intent, state: State) {
            if (!remoteServerManager.isRunning()) {
                coroutines.mainScope.launch {
                    remoteServerManager.start()
                    // fixme limit?
                    while (remoteServerManager.getService()?.isServerStarted != true) delay(20)
                    log.d("isRunning ${remoteServerManager.isRunning()} svc: ${remoteServerManager.getService()} address: ${remoteServerManager.getService()?.localNode?.http()}")
                    dispatch(Result.UpdateServerState)
                }
                remotesRepository.updatesCallback = { remoteNodes ->
                    log.d("remotes callback: ${remoteNodes.size}")
                    coroutines.mainScope.launch {
                        dispatch(Result.SetNodes(remoteNodes))
                    }
                }
            }
        }

        private fun testLoad() {
            dispatch(Result.UpdateServerState)
        }
    }

    fun create(): MviStore =
        object : MviStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RemotesStore",
            initialState = State(
                localNode = localRepository.getLocalNode(),
                wifiState = connectivityWrapper.getWIFIInfo()
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl()
        ) {}


}