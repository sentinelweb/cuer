package uk.co.sentinelweb.cuer.app.ui.player

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerStoreFactory.Action.InitSkipTimes
import uk.co.sentinelweb.cuer.app.ui.player.PlayerStoreFactory.Action.Playlist
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlayerStoreFactory(
    private val storeFactory: StoreFactory,
    private val itemLoader: PlayerContract.PlaylistItemLoader,
    private val queueConsumer: QueueMediatorContract.Consumer,
    private val queueProducer: QueueMediatorContract.Producer,
    private val skip: SkipContract.External,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper
) {

    private sealed class Result {
        object NoVideo : Result()
        class State(val state: PlayerStateDomain) : Result()
        class LoadVideo(val item: PlaylistItemDomain, val playlist: PlaylistDomain? = null) : Result()
        class Playlist(val playlist: PlaylistDomain) : Result()
        class SkipTimes(val fwd: String? = null, val back: String? = null) : Result()
    }

    private sealed class Action {
        class Playlist(val playlist: PlaylistDomain) : Action()
        object InitSkipTimes : Action()
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.State -> copy(playerState = result.state)
                is Result.LoadVideo -> copy(item = result.item, playlist = result.playlist ?: playlist)
                is Result.Playlist -> copy(playlist = result.playlist)
                is Result.NoVideo -> copy(item = null)
                is Result.SkipTimes -> copy(
                    skipFwdText = result.fwd ?: skipFwdText,
                    skipBackText = result.back ?: skipFwdText
                )
            }
    }

    private class BootstrapperImpl(private val queueConsumer: QueueMediatorContract.Consumer) : SuspendBootstrapper<Action>() {
        override suspend fun bootstrap() {
            queueConsumer.playlist?.apply { dispatch(Playlist(this)) }
            dispatch(InitSkipTimes)
        }
    }

    private inner class ExecutorImpl(
        private val itemLoader: PlayerContract.PlaylistItemLoader,
        private val skip: SkipContract.External,
    ) : SuspendExecutor<Intent, Action, State, Result, Label>(), SkipContract.Listener {

        init {
            skip.listener = this
        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                is Intent.Play -> dispatch(Result.State(PLAYING))
                is Intent.Pause -> dispatch(Result.State(PAUSED))
                is Intent.PlayState -> playStateChange(intent.state, getState().item)
                is Intent.Load -> loadVideo()
                is Intent.TrackChange -> trackChange(intent)
                is Intent.PlaylistChange -> dispatch(Result.Playlist(intent.item))
                is Intent.TrackFwd -> queueConsumer.nextItem()
                is Intent.TrackBack -> queueConsumer.previousItem()
                is Intent.SkipBack -> publish(Label.Command(SkipBack(skip.skipBackInterval)))// todo skip.skipBack() not working
                is Intent.SkipFwd -> publish(Label.Command(SkipFwd(skip.skipForwardInterval)))// todo skip.skipFwd() not working
                is Intent.Position -> updatePosition(intent.ms, getState().item)
                is Intent.SkipFwdSelect -> skip.onSelectSkipTime(true)
                is Intent.SkipBackSelect -> skip.onSelectSkipTime(false)
                is Intent.PlayPause -> publish(Label.Command(if (intent.isPlaying) Pause else Play))
                is Intent.SeekTo -> seekTo(intent.fraction, getState().item)
                is Intent.PlaylistView -> Unit // todo
                is Intent.PlaylistItemView -> Unit // todo
            }

        private fun seekTo(fraction: Float, item: PlaylistItemDomain?) {
            item?.media?.duration?.let { dur -> publish(Label.Command(SeekTo(ms = (fraction * dur).toLong()))) }
        }

        private fun trackChange(intent: Intent.TrackChange) {
            intent.item.media.duration?.apply { skip.duration = this }
            dispatch(Result.LoadVideo(intent.item, queueConsumer.playlist))
        }

        private fun updatePosition(ms: Int, item: PlaylistItemDomain?) {
            item
                ?.run { copy(media = media.copy(positon = ms.toLong())) }
                ?.apply { queueConsumer.updateCurrentMediaItem(media) }
                ?.apply { skip.updatePosition(ms.toLong()) }
                ?.run { dispatch(Result.LoadVideo(this)) }
        }

        override suspend fun executeAction(action: Action, getState: () -> State) =
            when (action) {
                is Playlist -> dispatch(Result.Playlist(action.playlist))
                InitSkipTimes -> dispatch(Result.SkipTimes(skip.skipForwardText, skip.skipBackText))
            }

        private fun playStateChange(playState: PlayerStateDomain, item: PlaylistItemDomain?) =
            when (playState) {
                VIDEO_CUED -> {
                    item?.media?.positon
                        ?.takeIf { pos -> pos > 0 && item.media.duration?.let { pos < it - 10000 } ?: false }
                        ?.also { publish(Label.Command(SeekTo(it))) }
                    Play
                }
                ENDED -> {
                    queueConsumer.onTrackEnded()
                    None
                }
                else -> None
            }.let {
                skip.stateChange(playState)
                dispatch(Result.State(playState))
                publish(Label.Command(it))
            }

        private suspend fun loadVideo() {
            withContext(coroutines.Computation) {
                itemLoader.load()
                    ?.also { item ->
                        item.playlistId?.toIdentifier(OrchestratorContract.Source.LOCAL)
                            ?.apply { queueProducer.playNow(this, item.id) }
                    }
            }
                ?.apply { dispatch(Result.LoadVideo(this, null)) }
                ?: apply { dispatch(Result.NoVideo) }
        }

        override fun skipSeekTo(target: Long) {
            publish(Label.Command(SeekTo(target)))
        }

        override fun skipSetBackText(text: String) {
            dispatch(Result.SkipTimes(back = text))
        }

        override fun skipSetFwdText(text: String) {
            dispatch(Result.SkipTimes(fwd = text))
        }
    }

    fun create(): PlayerContract.MviStore =
        object : PlayerContract.MviStore, Store<Intent, State, Label> by storeFactory.create(
            name = "PlayerStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(queueConsumer),
            executorFactory = { ExecutorImpl(itemLoader, skip) },
            reducer = ReducerImpl
        ) {
        }
}