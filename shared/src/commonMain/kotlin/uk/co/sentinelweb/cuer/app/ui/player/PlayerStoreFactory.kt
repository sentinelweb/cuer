package uk.co.sentinelweb.cuer.app.ui.player

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.State
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.PAUSED
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.PLAYING
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlayerStoreFactory(
    private val storeFactory: StoreFactory,
    private val itemLoader: PlayerContract.PlaylistItemLoader,
    private val idParser: PlayerContract.PlaylistItemIdParser
) {
    private sealed class Result {
        class State(val state: PlayerStateDomain) : Result()
        class LoadVideo(val item: PlaylistItemDomain) : Result()
    }

    private sealed class Action {
        class LoadVideo(val id: Long) : Action()
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.State -> copy(state = result.state)
                is Result.LoadVideo -> copy(item = result.item)
            }
    }

    private class ExecutorImpl(
        private val itemLoader: PlayerContract.PlaylistItemLoader
    ) : SuspendExecutor<Intent, Action, State, Result, Nothing>() {
        override suspend fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                is Intent.Play -> dispatch(Result.State(PLAYING))
                is Intent.Pause -> dispatch(Result.State(PAUSED))
                is Intent.PlayState -> dispatch(Result.State(intent.state))
                is Intent.LoadVideo -> loadVideo(intent.playlistItemId)
            }

        override suspend fun executeAction(action: Action, getState: () -> State) =
            when (action) {
                is Action.LoadVideo -> loadVideo(action.id)
            }

        private suspend fun loadVideo(id: Long) {
            val item = withContext(Dispatchers.Default) {
                itemLoader.load(id)
            }
            dispatch(Result.LoadVideo(item))
        }
    }

    private class BootstrapperImpl(
        private val idParser: PlayerContract.PlaylistItemIdParser
    ) : SuspendBootstrapper<Action>() {
        override suspend fun bootstrap() {
            val id = withContext(Dispatchers.Default) { idParser.id }
            dispatch(Action.LoadVideo(id))
        }
    }

    fun create(): PlayerContract.MviStore =
        object : PlayerContract.MviStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "PlayerStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(idParser),
            executorFactory = { ExecutorImpl(itemLoader) },
            reducer = ReducerImpl
        ) {
        }

}