package uk.co.sentinelweb.cuer.app.ui.local

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder

import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.MviStore
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.MviStore.*
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.ServerState
import uk.co.sentinelweb.cuer.remote.server.http

class LocalStoreFactory constructor(
    private val storeFactory: StoreFactory = DefaultStoreFactory(),
    private val strings: StringDecoder,
    private val log: LogWrapper,
    private val prefs: MultiPlatformPreferencesWrapper,
    private val remoteServerManager: RemoteServerContract.Manager,
    private val localRepository: LocalRepository,
    private val coroutines: CoroutineContextProvider,
    private val connectivityWrapper: ConnectivityWrapper,
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
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.UpdateServerState -> copy(
                    serverState = if (remoteServerManager.isRunning()) ServerState.STARTED else ServerState.STOPPED,
                    serverAddress = remoteServerManager.getService()?.localNode?.http(),
                    localNode = localRepository.getLocalNode(),
                    wifiState = connectivityWrapper.getWIFIInfo(),
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
                is Intent.ActionSave -> save(intent, getState())
            }

        private fun save(intent: Intent.ActionSave, state: State) {
            localRepository.getLocalNode()
                .copy(
                    hostname = intent.updated.hostname,
                    port = intent.updated.port,
                    authConfig = intent.updated.authConfig,
                    wifiAutoStart = intent.updated.wifiAutoStart,
                    wifiAutoNotify = intent.updated.wifiAutoNotify,
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
            initialState = State(wifiState = connectivityWrapper.getWIFIInfo()),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl()
        ) {}


}