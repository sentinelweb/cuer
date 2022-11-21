package uk.co.sentinelweb.cuer.app.ui.playlists

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.MviStore.*
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviStoreFactory.Action.Init
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class PlaylistsMviStoreFactory(
    private val storeFactory: StoreFactory,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper,

    ) {

    private sealed class Result {
        object Empty : Result()// todo remove
//        object NoVideo : Result()
//        data class State(val state: PlayerStateDomain) : Result()
//        data class SetVideo(val item: PlaylistItemDomain, val playlist: PlaylistDomain? = null) : Result()
//
//        data class Playlist(val playlist: PlaylistDomain) : Result()
//        data class SkipTimes(val fwd: String? = null, val back: String? = null) : Result()
//        data class Screen(val screen: PlayerContract.MviStore.Screen) : Result()
//        data class Position(val pos: Long) : Result()
    }

    private sealed class Action {
        object Init : Action()
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
//                is State -> copy(playerState = result.state)
                else -> copy()
            }
    }

    private class BootstrapperImpl() :
        CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Init)
        }
    }

    private inner class ExecutorImpl
        : CoroutineExecutor<Intent, Action, State, Result, Label>() {

        override fun executeAction(action: Action, getState: () -> State) =
            when (action) {
                Init -> initialize()
            }

        override fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                else -> dispatch(Result.Empty)
            }

        fun initialize() {
            // todo load
        }
    }

    fun create(): PlaylistsMviContract.MviStore =
        object : PlaylistsMviContract.MviStore, Store<Intent, State, Label> by storeFactory.create(
            name = "PlaylistsMviContract",
            initialState = State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl
        ) {}
}
