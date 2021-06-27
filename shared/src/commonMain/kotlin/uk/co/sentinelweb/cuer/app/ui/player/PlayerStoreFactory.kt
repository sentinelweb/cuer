package uk.co.sentinelweb.cuer.app.ui.player

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.*
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
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
        class LoadVideo(val item: PlaylistItemDomain, val playlist: PlaylistDomain?) : Result()
        class Playlist(val playlist: PlaylistDomain) : Result()
    }

    private sealed class Action {
        class Playlist(val playlist: PlaylistDomain) : Action()
    }


    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.State -> copy(playerState = result.state)
                is Result.LoadVideo -> copy(item = result.item, playlist = result.playlist ?: playlist)
                is Result.Playlist -> copy(playlist = result.playlist)
                is Result.NoVideo -> copy(item = null)
            }
    }

    private class BootstrapperImpl(private val queueConsumer: QueueMediatorContract.Consumer) : SuspendBootstrapper<Action>() {
        override suspend fun bootstrap() {
            queueConsumer.playlist?.apply { dispatch(Action.Playlist(this)) }
        }
    }

    private inner class ExecutorImpl(
        private val itemLoader: PlayerContract.PlaylistItemLoader
    ) : SuspendExecutor<Intent, Action, State, Result, Label>() {
        override suspend fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                is Intent.Play -> dispatch(Result.State(PLAYING))
                is Intent.Pause -> dispatch(Result.State(PAUSED))
                is Intent.PlayState -> checkCommand(intent.state, getState().item)
                is Intent.Load -> loadVideo()
                is Intent.TrackChange -> {
                    dispatch(Result.LoadVideo(intent.item, queueConsumer.playlist))
                }
                is Intent.PlaylistChange -> dispatch(Result.Playlist(intent.item))
                is Intent.TrackFwd -> queueConsumer.nextItem()
                is Intent.TrackBack -> queueConsumer.previousItem()
                is Intent.SkipBack -> publish(Label.Command(SkipBack(30000)))
                is Intent.SkipFwd -> publish(Label.Command(SkipFwd(30000)))
            }

        override suspend fun executeAction(action: Action, getState: () -> State) =
            when (action) {
                is Action.Playlist -> dispatch(Result.Playlist(action.playlist))
            }

        private fun checkCommand(playState: PlayerStateDomain, item: PlaylistItemDomain?) =
            when (playState) {
                VIDEO_CUED -> {
                    item?.media?.positon
                        ?.takeIf { pos -> pos > 0 && item.media.duration?.let { pos < it - 10000 } ?: false }
                        ?.also { publish(Label.Command(JumpTo(it))) }
                    Play
                }
                ENDED -> {
                    queueConsumer.onTrackEnded()
                    None
                }
                else -> None
            }.let {
                dispatch(Result.State(playState))
                publish(Label.Command(it))
            }

        private suspend fun loadVideo() {
            withContext(Dispatchers.Default) {
                itemLoader.load()
            }
                ?.apply { dispatch(Result.LoadVideo(this, null)) }
                ?: apply { dispatch(Result.NoVideo) }
        }
    }

    fun create(): PlayerContract.MviStore =
        object : PlayerContract.MviStore, Store<Intent, State, Label> by storeFactory.create(
            name = "PlayerStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(queueConsumer),
            executorFactory = { ExecutorImpl(itemLoader) },
            reducer = ReducerImpl
        ) {
        }
}