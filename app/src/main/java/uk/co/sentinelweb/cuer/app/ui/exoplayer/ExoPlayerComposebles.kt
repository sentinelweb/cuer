package uk.co.sentinelweb.cuer.app.ui.exoplayer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import httpLocalNetworkUrl
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedTheme
import uk.co.sentinelweb.cuer.app.ui.player.PlayerComposeables
import uk.co.sentinelweb.cuer.app.ui.player.PlayerComposeables.PlayerTransport
import uk.co.sentinelweb.cuer.app.ui.player.PlayerComposeables.VolumeDisplay
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.Command
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.None
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.remote.server.LocalRepository

private const val LOG_TAG = "ExoPlayerComposebles"

private const val HIDE_CONTROLS_TIMEOUT = 3000L

object ExoPlayerComposebles : KoinComponent {

    private val localRepository: LocalRepository by inject()

    @Composable
    fun ExoPlayerUi(view: ExoActivity.MviViewImpl) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val state = view.model.collectAsState()
        val label = view.labelFlow.collectAsState(None)

        var aspectRatioState by remember { mutableStateOf(1f) }
        var controlsVisible by remember { mutableStateOf(true) }
        var volumeVisible by remember { mutableStateOf(true) }

        val exoPlayer = remember {
            ExoPlayer.Builder(context).build().apply {
                prepare()
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_ONE
                addListener(exoPlayerListener(view, this))
                addListener(object : Player.Listener {
                    override fun onVideoSizeChanged(size: VideoSize) {
                        if (size.height != 0) {
                            aspectRatioState =
                                (size.width.toFloat() / size.height.toFloat()) * size.pixelWidthHeightRatio
                        }
                    }
                })
            }
        }

        LaunchedEffect(label.value) {
            Log.d(LOG_TAG, "playState = ${state.value.playState}")
            when (label.value) {
                is Command -> processCommand((label.value as Command).command, exoPlayer)
                else -> Unit
            }
        }

        LaunchedEffect(exoPlayer) {
            while (true) {
                view.dispatch(PositionReceived(exoPlayer.currentPosition))
                delay(1000L)
            }
        }

        LaunchedEffect(state.value.volume) {
            exoPlayer.volume = state.value.volume
                .also { volumeVisible = true }
                .also { Log.d(LOG_TAG, "set volume = $it") }
        }

        LaunchedEffect(Unit) {
            while (true) {
                if (controlsVisible) {
                    delay(HIDE_CONTROLS_TIMEOUT) // 3 seconds delay
                    controlsVisible = false
                } else {
                    delay(200L) // Short delay to keep checking
                }
            }
        }

        LaunchedEffect(Unit) {
            while (true) {
                if (volumeVisible) {
                    delay(HIDE_CONTROLS_TIMEOUT) // 3 seconds delay
                    volumeVisible = false
                } else {
                    delay(200L) // Short delay to keep checking
                }
            }
        }

        DisposableEffect(lifecycleOwner, exoPlayer) {
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE ->
                        view.dispatch(PlayPauseClicked(true))

                    Lifecycle.Event.ON_RESUME -> {
                        // fixme not resuming properly stte needs to be updted but doesnt seem to .. caching?
                        view.dispatch(PlayPauseClicked(exoPlayer.isPlaying))
                    }

                    Lifecycle.Event.ON_DESTROY -> exoPlayer.release()
                    else -> {}
                }
            }

            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
                exoPlayer.release()
            }
        }

        CuerSharedTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                PlayerSurface(
                    player = exoPlayer,
                    surfaceType = SURFACE_TYPE_SURFACE_VIEW,
                    aspectRatio = aspectRatioState,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                controlsVisible = !controlsVisible
                                volumeVisible = !volumeVisible
                            })
                        },
                )
                if (controlsVisible) {
                    PlayerTransport(
                        model = state.value,
                        view = view,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
                if (volumeVisible) {
                    VolumeDisplay(exoPlayer.volume, modifier = Modifier.Companion.align(Alignment.TopEnd))
                }
            }
        }
    }

    fun processCommand(command: PlayerCommand, exoPlayer: ExoPlayer) {
        Log.d(LOG_TAG, "command = $command")
        when (command) {
            is Load -> command.item.httpLocalNetworkUrl(localRepository)
                //?.also { Log.d(LOG_TAG, "setMedia = $it") }
                ?.also { exoPlayer.setMediaItem(MediaItem.fromUri(it)) }

            Pause -> exoPlayer.pause()
            Play -> exoPlayer.play()
            is SeekTo -> exoPlayer.seekTo(command.ms)
            is SkipBack -> exoPlayer.seekTo(exoPlayer.currentPosition - command.ms)
            is SkipFwd -> exoPlayer.seekTo(exoPlayer.currentPosition + command.ms)
        }
    }


    private fun exoPlayerListener(
        view: ExoActivity.MviViewImpl,
        player: ExoPlayer
    ) = object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {

                Player.STATE_READY -> {
                    Log.d(LOG_TAG, "Media is loaded and ready to play")
                    view.dispatch(DurationReceived(player.duration))
//                    view.dispatch(PlayerStateChanged(VIDEO_CUED))
                }

                Player.STATE_BUFFERING -> {
                    Log.d(LOG_TAG, "Media is buffering")
                    view.dispatch(PlayerStateChanged(BUFFERING))
                }

                Player.STATE_ENDED -> {
                    Log.d(LOG_TAG, "Media has ended")
                    view.dispatch(PlayerStateChanged(ENDED))
                }

                Player.STATE_IDLE -> {
                    view.dispatch(PlayerStateChanged(UNSTARTED))
                    Log.d(LOG_TAG, "ExoPlayer is idle")
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val state = if (isPlaying) PLAYING else PAUSED
            if (isPlaying) {
                Log.d(LOG_TAG, "Playback started")
                view.dispatch(PlayerStateChanged(state))
            } else {
                Log.d(LOG_TAG, "Playback paused")
                view.dispatch(PlayerStateChanged(state))
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(LOG_TAG, "Playback paused", error)
            view.dispatch(PlayerStateChanged(ERROR))
        }
    }
}
