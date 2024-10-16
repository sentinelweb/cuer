package uk.co.sentinelweb.cuer.app.ui.player

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.service.remote.player.PlayerSessionListener
import uk.co.sentinelweb.cuer.app.service.remote.player.PlayerSessionManager
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerStoreFactory.Action.*
import uk.co.sentinelweb.cuer.app.util.android_yt_player.live.LivePlaybackContract
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionContract
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.ignoreJob
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.domain.ext.startPosition

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
    private val mediaSessionListener: MediaSessionListener,
    private val mediaOrchestrator: MediaOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val playerSessionManager: PlayerSessionManager,
    private val playerSessionListener: PlayerSessionListener,
    private val config: PlayerContract.PlayerConfig,
    private val prefs: MultiPlatformPreferencesWrapper,
) {
    init {
        log.tag(this)
    }

    private sealed class Result {
        object NoVideo : Result()
        data class State(val state: PlayerStateDomain) : Result()
        data class SetVideo(val item: PlaylistItemDomain, val playlist: PlaylistDomain? = null) : Result()
        data class Playlist(val playlist: PlaylistDomain) : Result()
        data class SkipTimes(val fwd: String? = null, val back: String? = null) : Result()
        data class SelectedTab(val content: Content) : Result()
        data class Position(val pos: Long) : Result()
        data class Volume(val vol: Float) : Result()
        data class Screen(val screen: PlayerNodeDomain.Screen) : Result()
    }

    private sealed class Action {
        class Playlist(val playlist: PlaylistDomain) : Action()
        object InitSkipTimes : Action()
        object Init : Action()
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(msg: Result): State =
            when (msg) {
                is Result.State -> copy(playerState = msg.state)
                is Result.SetVideo -> copy(
                    item = msg.item,
                    playlist = msg.playlist ?: playlist
                )

                is Result.Playlist -> copy(playlist = msg.playlist)
                is Result.NoVideo -> copy(item = null)
                is Result.SelectedTab -> copy(content = msg.content)
                is Result.SkipTimes -> copy(
                    skipFwdText = msg.fwd ?: skipFwdText,
                    skipBackText = msg.back ?: skipBackText
                )

                is Result.Position -> copy(position = msg.pos)
                is Result.Volume -> copy(volume = msg.vol)
                is Result.Screen -> copy(screen = screen)
            }//.also { println("PlayerStoreFactory: volume=${it.volume} msg=$msg") }
    }

    private class BootstrapperImpl(private val queueConsumer: QueueMediatorContract.Consumer) :
        CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Init)
            queueConsumer.playlist?.apply { dispatch(Playlist(this)) }
            dispatch(InitSkipTimes)
        }
    }

    private inner class ExecutorImpl(
        private val itemLoader: PlayerContract.PlaylistItemLoader,
        private val skip: SkipContract.External,
    ) : CoroutineExecutor<Intent, Action, State, Result, Label>(), SkipContract.Listener {

        init {
            skip.listener = this
        }

        override fun executeAction(action: Action, getState: () -> State) =
            when (action) {
                is Playlist -> dispatch(Result.Playlist(action.playlist))
                InitSkipTimes -> dispatch(Result.SkipTimes(skip.skipForwardText, skip.skipBackText))
                Init -> init()
            }

        override fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                is Intent.PlayState -> playStateChange(intent.state, getState().item)
                is Intent.TrackChange -> trackChange(intent)
                is Intent.PlaylistChange -> dispatch(Result.Playlist(intent.item))
                is Intent.TrackFwd -> queueConsumer.nextItem()
                is Intent.TrackBack -> queueConsumer.previousItem()
                is Intent.SkipBack -> skip.skipBack()
                is Intent.SkipFwd -> skip.skipFwd()
                is Intent.Position -> updatePosition(intent.ms, getState().item, getState().playerState)
                is Intent.SkipFwdSelect -> skip.onSelectSkipTime(true)
                is Intent.SkipBackSelect -> skip.onSelectSkipTime(false)
                is Intent.PlayPause -> playPause(intent, getState().playerState)
                is Intent.SeekToFraction -> seekTo(intent.fraction, getState().item)
                is Intent.PlaylistView -> dispatch(Result.SelectedTab(Content.PLAYLIST))
                is Intent.PlaylistItemView -> dispatch(Result.SelectedTab(Content.DESCRIPTION))
                is Intent.LinkOpen -> publish(Label.LinkOpen(intent.link))
                is Intent.ChannelOpen -> openChannel(getState())
                is Intent.TrackSelected -> trackSelected(intent.item, intent.resetPosition)
                is Intent.Duration -> receiveDuration(intent)
                is Intent.Id -> livePlaybackController.gotVideoId(intent.videoId)
                is Intent.FullScreenPlayerOpen -> publish(Label.FullScreenPlayerOpen(getState().playlistAndItem()!!))
                is Intent.PortraitPlayerOpen -> publish(Label.PortraitPlayerOpen(getState().playlistAndItem()!!))
                is Intent.PipPlayerOpen -> publish(Label.PipPlayerOpen(getState().playlistAndItem()!!))
                is Intent.SeekToPosition -> publish(Label.Command(SeekTo(ms = intent.ms)))
                is Intent.InitFromService -> initFromService(intent)
                is Intent.PlayItemFromService -> playItemFromService(intent)
                Intent.Support -> getState().item?.let { publish(Label.ShowSupport(it)) } ?: Unit
                Intent.StarClick -> toggleStar(getState().item).let { Unit }
                is Intent.OpenInApp -> getState().item?.let { publish(Label.ItemOpen(it)) } ?: Unit
                is Intent.Share -> getState().item?.let { publish(Label.Share(it)) } ?: Unit
                Intent.Stop -> publish(Label.Stop)
                Intent.FocusWindow -> publish(Label.FocusWindow)
                is Intent.VolumeChanged -> volumeChanged(intent)
                is Intent.ScreenAcquired -> screenAcquired(intent)
            }

        private fun screenAcquired(intent: Intent.ScreenAcquired) {
            playerSessionManager.setScreen(intent.screen)
            dispatch(Result.Screen(intent.screen))
        }

        private fun volumeChanged(intent: Intent.VolumeChanged) {
            log.d("volumeChanged: ${intent.vol}")
            prefs.volume = intent.vol
            playerSessionManager.setVolume(intent.vol)
            dispatch(Result.Volume(intent.vol))
        }

        private fun receiveDuration(intent: Intent.Duration) {
            livePlaybackController.gotDuration(intent.ms)
            skip.duration = intent.ms
            queueConsumer.currentItem
                ?.takeIf { it.media.duration == null || it.media.duration != intent.ms }
                ?.let { it.copy(media = it.media.copy(duration = intent.ms)) }
                ?.also { queueConsumer.updateCurrentMediaItem(it.media) }
                ?.apply { dispatch(Result.SetVideo(this)) }
        }

        private fun openChannel(state: State) =
            state.item?.media?.channelData
                ?.let { publish(Label.ChannelOpen(it)) }
                ?: Unit

        private fun initFromService(intent: Intent.InitFromService) {
            loadItem(intent.playlistAndItem)
            queueConsumer.playlist
                ?.also { dispatch(Result.Playlist(it)) }
            Unit
        }

        private fun playItemFromService(intent: Intent.PlayItemFromService) {
            coroutines.mainScope.launch {
                log.d("playItemFromService: playlistId${intent.playlistAndItem.playlistId} playlistTitle:${intent.playlistAndItem.playlistTitle} itemId:${intent.playlistAndItem.item.id} itemTitle:${intent.playlistAndItem.item.media.title}")
                queueProducer.playNow(intent.playlistAndItem.playlistId!!, intent.playlistAndItem.item.id)
            }
        }

        private fun toggleStar(item: PlaylistItemDomain?) = coroutines.mainScope.launch {
            item?.takeIf { it.id != null }
                ?.also {
                    // can do queueProducer.playlistId.deepOptions()
                    mediaOrchestrator.save(
                        it.media.copy(starred = it.media.starred.not()),
                        LOCAL.deepOptions(true)
                    )
                    playlistItemOrchestrator.loadById(it.id!!.id, LOCAL.deepOptions(false))
                        ?.apply { dispatch(Result.SetVideo(this)) }
                }
        }

        private fun init() = coroutines.mainScope.launch {
            // fixme there seem to be some race condition when binding the store to the UI so the initial load command
            // label doesnt make it to the view.processLabel() - this delay gets around it
            delay(1)
            log.d("init(): prefs.volume:${prefs.volume}")
            dispatch(Result.Volume(prefs.volume))
            itemLoader.load()?.also { playlistAndItem ->
                log.d("itemLoader.load(${playlistAndItem.item.media.title}))")
                loadItem(playlistAndItem)
            }
        }.ignoreJob()

        private fun loadItem(playlistAndItem: PlaylistAndItemDomain) = coroutines.mainScope.launch {
            playlistAndItem
                .apply { mediaSessionManager.checkCreateMediaSession(mediaSessionListener) }
                .apply { playerSessionManager.checkCreateMediaSession(playerSessionListener) }
                .apply { playerSessionManager.setItem(item, null) }
                //.apply { log.d("config.maxVolume: ${config.maxVolume}") }
                .apply { playerSessionManager.setVolumeMax(config.maxVolume) }
                .apply { playerSessionManager.setVolume(prefs.volume) }
                .apply { livePlaybackController.clear(item.media.platformId) }
                .apply { queueProducer.playNow(playlistId!!, item.id) }
        }

        private fun playPause(intent: Intent.PlayPause, playerState: PlayerStateDomain) {
            publish(
                Label.Command(if (intent.isPlaying ?: (playerState == PLAYING)) Pause else Play)
            )
        }

        private fun seekTo(fraction: Float, item: PlaylistItemDomain?) {
            item?.media?.duration
                ?.let { dur -> publish(Label.Command(SeekTo(ms = (fraction * dur).toLong()))) }
        }

        private fun trackChange(intent: Intent.TrackChange) {
            log.d("trackchange: ${intent.item.media.platformId}")
            intent.item.media.duration?.apply { skip.duration = this }
            livePlaybackController.clear(intent.item.media.platformId)

            mediaSessionManager.checkCreateMediaSession(mediaSessionListener)
            mediaSessionManager.setMedia(intent.item.media, queueConsumer.playlist)

            playerSessionManager.checkCreateMediaSession(playerSessionListener)
            playerSessionManager.setVolumeMax(config.maxVolume)
            playerSessionManager.setItem(intent.item, queueConsumer.playlist)
            dispatch(Result.SetVideo(intent.item, queueConsumer.playlist))
            publish(
                Label.Command(
                    Load(intent.item, intent.item.media.startPosition())
                )
            )
        }

        private fun updatePosition(
            ms: Long,
            item: PlaylistItemDomain?,
            playerState: PlayerStateDomain
        ) {
            item
                ?.run { copy(media = media.copy(positon = ms)) }
                ?.apply { queueConsumer.updateCurrentMediaItem(media) }
                ?.apply { skip.updatePosition(ms) }
                ?.apply { livePlaybackController.setCurrentPosition(ms) }
                ?.apply { updatePlaybackState(playerState) }
                ?.apply { dispatch(Result.SetVideo(this)) }
                ?.apply {
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
                        publish(Label.Command(Load(item, startPosition())))
                    }
                    Unit
                }

                ENDED -> {
                    queueConsumer.onTrackEnded()
                }

                ERROR -> {
                    queueConsumer.nextItem()
                }

                else -> Unit
            }.also {
                item?.apply {
                    updatePlaybackState(playState)
                }

                skip.stateChange(playState)
                dispatch(Result.State(playState))
            }

        private fun PlaylistItemDomain.updatePlaybackState(playState: PlayerStateDomain) {
            mediaSessionManager.updatePlaybackState(
                this.media,
                playState,
                if (media.isLiveBroadcast) livePlaybackController.getLiveOffsetMs() else null,
                queueConsumer.playlist
            )
            playerSessionManager.updatePlaybackState(
                this,
                playState,
                if (media.isLiveBroadcast) livePlaybackController.getLiveOffsetMs() else null,
                queueConsumer.playlist
            )
        }

        private fun trackSelected(item: PlaylistItemDomain, resetPosition: Boolean) {
            item.playlistId
                ?.apply { livePlaybackController.clear(item.media.platformId) }
                ?.apply { queueProducer.onItemSelected(item, false, resetPosition) }
        }

        override fun skipSeekTo(target: Long) {
            log.d("skip presenter seekTo:${target}")
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
            override fun endSession() {
                mediaSessionManager.destroyMediaSession()
                playerSessionManager.destroyMediaSession()
            }
        }
}
