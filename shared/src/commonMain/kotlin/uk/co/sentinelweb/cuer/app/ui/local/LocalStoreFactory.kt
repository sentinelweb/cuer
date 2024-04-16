package uk.co.sentinelweb.cuer.app.ui.local

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract

import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.MviStore
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.MviStore.*
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.ServerState
import uk.co.sentinelweb.cuer.remote.server.http

class LocalStoreFactory constructor(
    private val storeFactory: StoreFactory = DefaultStoreFactory(),
    private val log: LogWrapper,
    private val remoteServerManager: RemoteServerContract.Manager,
    private val localRepository: LocalRepository,
    private val connectivityWrapper: ConnectivityWrapper,
    private val wifiStateProvider: WifiStateProvider,
) {

    init {
        log.tag(this)
    }

    private sealed class Result {
        object UpdateServerState : Result()
    }

    private sealed class Action {
        object Init : Action()
    }

    private inner class ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(msg: Result): State =
            when (msg) {
                is Result.UpdateServerState -> copy(
                    serverState = if (remoteServerManager.isRunning()) ServerState.STARTED else ServerState.STOPPED,
                    serverAddress = remoteServerManager.getService()?.localNode?.http(),
                    localNode = localRepository.localNode,
                    wifiState = wifiStateProvider.wifiState,
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
            when (intent) {
                Intent.Up -> publish(Label.Up)
                is Intent.ActionSave -> save(intent)
            }

        private fun save(intent: Intent.ActionSave) {
            localRepository.localNode
                .copy(
                    hostname = intent.updated.hostname,
                    port = intent.updated.port,
                    authConfig = intent.updated.authConfig,
                    wifiAutoStart = intent.updated.wifiAutoStart,
                    wifiAutoConnectSSIDs = intent.updated.wifiAutoConnectSSIDs,
                )
                .also { localRepository.saveLocalNode(it) }

            dispatch(Result.UpdateServerState)
            publish(Label.Saved)
        }

        private fun testLoad() {
            dispatch(Result.UpdateServerState)
        }

    }

    fun create(): MviStore =
        object : MviStore, Store<Intent, State, Label> by storeFactory.create(
            name = "LocalStore",
            initialState = State(wifiState = wifiStateProvider.wifiState),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl()
        ) {}
}
