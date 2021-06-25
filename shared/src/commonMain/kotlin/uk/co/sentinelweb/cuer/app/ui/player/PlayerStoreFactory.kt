package uk.co.sentinelweb.cuer.app.ui.player

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.State
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.*
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlayerStoreFactory(
    private val storeFactory: StoreFactory,
    private val itemLoader: PlayerContract.PlaylistItemLoader,
    private val queueConsumer: QueueMediatorContract.Consumer,
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
                is Intent.Load -> loadVideo()
                is Intent.TrackChange -> dispatch(Result.LoadVideo(intent.item))
                is Intent.TrackFwd -> queueConsumer.nextItem()
                is Intent.TrackBack -> queueConsumer.previousItem()
                is Intent.SkipBack -> dispatch(Result.Command(SkipBack(30000)))
                is Intent.SkipFwd -> dispatch(Result.Command(SkipFwd(30000)))
            }

        private fun checkCommand(playState: PlayerStateDomain) =
            when (playState) {
                VIDEO_CUED -> Play
                ENDED -> {
                    queueConsumer.onTrackEnded()
                    None
                }
                else -> None
            }.let { dispatch(Result.Command(it, playState)) }


        private suspend fun loadVideo() {
            withContext(Dispatchers.Default) {
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