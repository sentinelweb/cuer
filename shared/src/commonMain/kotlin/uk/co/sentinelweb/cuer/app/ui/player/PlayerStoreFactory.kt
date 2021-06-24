package uk.co.sentinelweb.cuer.app.ui.player

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.State
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.NONE
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.PLAY
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlayerStoreFactory(
    private val storeFactory: StoreFactory,
    private val itemLoader: PlayerContract.PlaylistItemLoader,
    private val log: LogWrapper
) {
    private sealed class Result {
        object NoVideo : Result()
        class State(val state: PlayerStateDomain) : Result()
        class Command(val command: PlayerContract.PlayerCommand, val state: PlayerStateDomain? = null) : Result()
        class LoadVideo(val item: PlaylistItemDomain) : Result()
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.State -> copy(state = result.state)
                is Result.Command -> copy(command = result.command, state = result.state ?: state)
                is Result.LoadVideo -> copy(item = result.item)
                is Result.NoVideo -> copy(item = null)
            }
    }

    private inner class ExecutorImpl(
        private val itemLoader: PlayerContract.PlaylistItemLoader
    ) : SuspendExecutor<Intent, Nothing /* Action */, State, Result, Nothing>() {
        override suspend fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                is Intent.Play -> dispatch(Result.State(PLAYING))
                is Intent.Pause -> dispatch(Result.State(PAUSED))
                is Intent.PlayState -> checkCommand(intent.state)
                is Intent.Load -> {
                    loadVideo()
                }
            }

        private fun checkCommand(state: PlayerStateDomain) =
            when (state) {
                VIDEO_CUED -> PLAY
                else -> NONE
            }.let { dispatch(Result.Command(it, state)) }


        private suspend fun loadVideo() {
            withContext(Dispatchers.Default) {
                //log.d("loadVideo()")
                itemLoader.load()
            }
                ?.apply { dispatch(Result.LoadVideo(this)) }
                ?: apply { dispatch(Result.NoVideo) }

        }
    }


    fun create(): PlayerContract.MviStore =
        object : PlayerContract.MviStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "PlayerStore",
            initialState = State(),
            executorFactory = { ExecutorImpl(itemLoader) },
            reducer = ReducerImpl
        ) {
        }
}