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
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.remote.server.ServerState

class LocalStoreFactory constructor(
    private val storeFactory: StoreFactory = DefaultStoreFactory(),
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
                //Intent.SendPing -> log.d("send ping")
                Intent.Up -> publish(Label.Up)
            }

        private fun testLoad() {
            //dispatch(Result.SetNodes(listOf()))
            dispatch(Result.UpdateServerState)
        }

    }

    fun create(): MviStore =
        object : MviStore, Store<Intent, State, Label> by storeFactory.create(
            name = "LocalStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl()
        ) {}


}