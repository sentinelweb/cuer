package uk.co.sentinelweb.cuer.app.ui.player

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerStoreFactory.Action.*
import uk.co.sentinelweb.cuer.app.util.android_yt_player.live.LivePlaybackContract
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
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
    private val log: LogWrapper,
    private val livePlaybackController: LivePlaybackContract.Controller,
    private val mediaSessionManager: MediaSessionContract.Manager,
    private val playerControls: PlayerContract.PlayerControls.Listener,

    ) {

    private sealed class Result {
        object NoVideo : Result()
        class State(val state: PlayerStateDomain) : Result()
        class SetVideo(val item: PlaylistItemDomain, val playlist: PlaylistDomain? = null) : Result()
        class Playlist(val playlist: PlaylistDomain) : Result()
        class SkipTimes(val fwd: String? = null, val back: String? = null) : Result()
        class Screen(val screen: PlayerContract.MviStore.Screen) : Result()
        class Position(val pos: Long) : Result()
    }

    private sealed class Action {
        class Playlist(val playlist: PlaylistDomain) : Action()
        object InitSkipTimes : Action()
        object Init : Action()
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.State -> copy(playerState = result.state)
                is Result.SetVideo -> copy(item = result.item, playlist = result.playlist ?: playlist)
                is Result.Playlist -> copy(playlist = result.playlist)
                is Result.NoVideo -> copy(item = null)
                is Result.Screen -> copy(screen = result.screen)
                is Result.SkipTimes -> copy(
                    skipFwdText = result.fwd ?: skipFwdText,
                    skipBackText = result.back ?: skipBackText
                )
                is Result.Position -> copy(position = result.pos)
            }
    }

    private class BootstrapperImpl(private val queueConsumer: QueueMediatorContract.Consumer) : SuspendBootstrapper<Action>() {
        override suspend fun bootstrap() {
            dispatch(Init)
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

        override suspend fun executeAction(action: Action, getState: () -> State) =
            when (action) {
                is Playlist -> dispatch(Result.Playlist(action.playlist))
                InitSkipTimes -> dispatch(Result.SkipTimes(skip.skipForwardText, skip.skipBackText))
                Init -> init()
            }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                is Intent.Play -> dispatch(Result.State(PLAYING))
                is Intent.Pause -> dispatch(Result.State(PAUSED))
                is Intent.PlayState -> playStateChange(intent.state, getState().item)
                is Intent.TrackChange -> trackChange(intent)
                is Intent.PlaylistChange -> dispatch(Result.Playlist(intent.item))
                is Intent.TrackFwd -> queueConsumer.nextItem()
                is Intent.TrackBack -> queueConsumer.previousItem()
                is Intent.SkipBack -> skip.skipBack()
                is Intent.SkipFwd -> skip.skipFwd()
                is Intent.Position -> updatePosition(intent.ms, getState().item)
                is Intent.SkipFwdSelect -> skip.onSelectSkipTime(true)
                is Intent.SkipBackSelect -> skip.onSelectSkipTime(false)
                is Intent.PlayPause -> playPause(intent, getState().playerState)
                is Intent.SeekTo -> seekTo(intent.fraction, getState().item)
                is Intent.PlaylistView -> dispatch(Result.Screen(Screen.PLAYLIST))
                is Intent.PlaylistItemView -> dispatch(Result.Screen(Screen.DESCRIPTION))
                is Intent.LinkOpen -> publish(Label.LinkOpen(intent.url))
                is Intent.ChannelOpen -> getState().item?.media?.channelData?.let { publish(Label.ChannelOpen(it)) } ?: Unit
                is Intent.TrackSelected -> trackSelected(intent.item, intent.resetPosition)
                is Intent.Duration -> livePlaybackController.gotDuration(intent.ms)
                is Intent.Id -> livePlaybackController.gotVideoId(intent.videoId)
                is Intent.FullScreenPlayerOpen -> publish(Label.FullScreenPlayerOpen(getState().item!!))
                is Intent.PortraitPlayerOpen -> publish(Label.PortraitPlayerOpen(getState().item!!))
                is Intent.PipPlayerOpen -> publish(Label.PipPlayerOpen(getState().item!!))
                is Intent.SeekToPosition -> publish(Label.Command(SeekTo(ms = intent.ms)))
            }

        private suspend fun init() {
            withContext(coroutines.Computation) {
                itemLoader.load()?.also { item ->
                    item.playlistId
                        ?.toIdentifier(LOCAL)
                        ?.apply { livePlaybackController.clear(item.media.platformId) }
                        ?.apply { queueProducer.playNow(this, item.id) }
                    coroutines.mainScope.launch {
                        mediaSessionManager.checkCreateMediaSession(playerControls)
                    }
                    log.d("itemLoader.load(${item.media.title}))")
                }
            }
        }

        private fun destroy() {
            mediaSessionManager.destroyMediaSession()
        }

        private fun playPause(intent: Intent.PlayPause, playerState: PlayerStateDomain) {
            publish(Label.Command(if (intent.isPlaying ?: playerState == PLAYING) Pause else Play))
        }

        private fun seekTo(fraction: Float, item: PlaylistItemDomain?) {
            item?.media?.duration
                ?.let { dur -> publish(Label.Command(SeekTo(ms = (fraction * dur).toLong()))) }
        }

        private fun trackChange(intent: Intent.TrackChange) {
            intent.item.media.duration?.apply { skip.duration = this }
            livePlaybackController.clear(intent.item.media.platformId)
            dispatch(Result.SetVideo(intent.item, queueConsumer.playlist))
            publish(Label.Command(Load(intent.item.media.platformId, intent.item.media.positon ?: 0)))
            //coroutines.mainScope.launch {
            mediaSessionManager.setMedia(intent.item.media, queueConsumer.playlist)
            //}
            log.d("trackChange(${intent.item.media.title}))")
        }

        private fun updatePosition(ms: Long, item: PlaylistItemDomain?) {
            item
                ?.run { copy(media = media.copy(positon = ms)) }
                ?.apply { queueConsumer.updateCurrentMediaItem(media) }
                ?.apply { skip.updatePosition(ms) }
                ?.apply { livePlaybackController.setCurrentPosition(ms) }
                ?.run { dispatch(Result.SetVideo(this)) }

                ?.run {
                    dispatch(
                        Result.Position(
                            if (item.media.isLiveBroadcast) livePlaybackController.getLiveOffsetMs() else ms
                        )
                    )
                }
        }

        private fun playStateChange(playState: PlayerStateDomain, item: PlaylistItemDomain?): Unit =
            when (playState) {
                VIDEO_CUED -> {
                    item?.media?.apply {
                        publish(Label.Command(Load(platformId, startPosition())))
                        //coroutines.mainScope.launch {
                        //    mediaSessionManager.setMedia(item.media, queueConsumer.playlist)
                        //}
                    }
                    Unit
                }
                ENDED -> {
                    queueConsumer.onTrackEnded()
                }
                else -> Unit
            }.also {
                item?.apply {
                    //coroutines.mainScope.launch {
                    mediaSessionManager.updatePlaybackState(
                        media,
                        playState,
                        if (media.isLiveBroadcast) livePlaybackController.getLiveOffsetMs() else null,
                        queueConsumer.playlist
                    )
                    // }
                }

                skip.stateChange(playState)
                dispatch(Result.State(playState))
            }

        private suspend fun trackSelected(item: PlaylistItemDomain, resetPosition: Boolean) {
            withContext(coroutines.Computation) {
                item.also { item ->
                    item.playlistId
                        ?.toIdentifier(LOCAL)
                        ?.apply { livePlaybackController.clear(item.media.platformId) }
                        ?.apply { queueProducer.onItemSelected(item, false, resetPosition) }
                }
            }
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

        fun MediaDomain.startPosition() = run {
            val position = positon ?: -1
            val duration = duration ?: -1
            if (position > 0 &&
                duration > 0 && position < duration - 10000
            ) {
                position
            } else {
                0
            }
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