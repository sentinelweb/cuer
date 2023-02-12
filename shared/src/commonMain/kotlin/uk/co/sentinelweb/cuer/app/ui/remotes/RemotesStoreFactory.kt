package uk.co.sentinelweb.cuer.app.ui.remotes

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistStatsOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.toGuidIdentifier
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore.*
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.NodeDomain

// fixme: 'SuspendBootstrapper<Action : Any>' is deprecated. Please use CoroutineBootstrapper
class RemotesStoreFactory constructor(
    private val storeFactory: StoreFactory = DefaultStoreFactory(),
    private val repository: RemotesRepository,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistStatsOrchestrator: PlaylistStatsOrchestrator,
    private val strings: StringDecoder,
    private val log: LogWrapper,
    private val prefs: MultiPlatformPreferencesWrapper,
    private val remoteServerManager: RemoteServerContract.Manager,
    private val coroutines: CoroutineContextProvider,
) {

    init {
        log.tag(this)
    }

    private sealed class Result {
        data class SetNodes(val nodes: List<NodeDomain>) : Result()
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
                    serverAddress = remoteServerManager.getService()?.address,
                    localNode = remoteServerManager.getService()?.localNode
                )
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
            when (intent.also { log.d(it.toString()) }) {
                Intent.ActionSettings -> publish(Label.ActionSettings)
                Intent.ActionPasteAdd -> publish(Label.ActionPasteAdd)
                Intent.ActionSearch -> publish(Label.ActionSearch)
                Intent.ActionHelp -> publish(Label.ActionHelp)
                //Intent.SendPing -> log.d("send ping")
                Intent.Up -> publish(Label.Up)
                Intent.ActionConfig -> config(intent, getState())
                Intent.ActionPing -> ping(intent, getState())
                Intent.ActionStartServer -> startServer(intent, getState())
                Intent.ActionStopServer -> stopServer(intent, getState())
            }

        private fun config(intent: Intent, state: State) {
            TODO("Not yet implemented")
        }

        private fun ping(intent: Intent, state: State) {
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
                }
            }
        }

        private fun startServer(intent: Intent, state: State) {
            if (!remoteServerManager.isRunning()) {// just check if the service exists
                remotesJob = coroutines.mainScope.launch {
                    remoteServerManager.start()
                    while (remoteServerManager.getService()?.isServerStarted != true) {// fixme limit?
                        delay(20)
                    }

                    log.d("isRunning ${remoteServerManager.isRunning()} svc: ${remoteServerManager.getService()} address: ${remoteServerManager.getService()?.address}")
                    dispatch(Result.UpdateServerState)
                    remoteServerManager.getService()?.remoteNodes?.collectLatest {
                        dispatch(Result.SetNodes(it))
                    }
                }
            }
        }

        private fun testLoad() {
            dispatch(
                Result.SetNodes(
                    listOf(
                        NodeDomain(
                            id = "".toGuidIdentifier(MEMORY),
                            ipAddress = "",
                            port = 8989,
                        )
                    )
                )
            )
        }

    }

    fun create(): MviStore =
        object : MviStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RemnotesStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl()
        ) {}


}