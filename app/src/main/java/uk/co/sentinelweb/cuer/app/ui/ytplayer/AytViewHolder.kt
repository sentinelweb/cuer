package uk.co.sentinelweb.cuer.app.ui.ytplayer

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.utils.FadeViewHelper
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.views.PlayYangProgress
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import kotlin.math.abs
import com.pierfrancescosoffritti.androidyoutubeplayer.R as aytR

class AytViewHolder(
    private val log: LogWrapper,
    private val playYangProgress: PlayYangProgress
) {
    init {
        log.tag(this)
    }

    private var _playerView: YouTubePlayerView? = null
    private var _mviView: BaseMviView<Model, Event>? = null
    private var _progressBar: ProgressBar? = null

    private var _player: YouTubePlayer? = null
    private var _lastPositionSec: Float = -1f
    private var _lastPositionSend: Float = -1f
    private var _currentVideoId: String? = null

    private var _isSwitching: Boolean = false

    var controlsView: ConstraintLayout? = null
    private var _fadeViewHelper: FadeViewHelper? = null

    var playerState: PlayerStateDomain = UNKNOWN
        private set
    val isPlaying get() = playerState == PLAYING

    val playerView: YouTubePlayerView?
        get() = _playerView

    @SuppressLint("InflateParams")
    fun create(context: Context, controls: Boolean) {
        _playerView =
            LayoutInflater.from(context).inflate(R.layout.view_ayt_video, null) as? YouTubePlayerView
        _playerView?.initialize(
            ytPlayerListener,
            IFramePlayerOptions.Builder()
                .controls(if (controls) 1 else 0)
                .build()
        )
    }

    fun addView(context: Context, parent: FrameLayout, mviView: BaseMviView<Model, Event>, controls: Boolean) {
        if (_playerView == null) create(context, controls)
        _mviView = mviView
        parent.addView(_playerView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))

        _progressBar = parent.getChildAt(0)
            .findViewById<ProgressBar>(aytR.id.progress)
            ?.apply { playYangProgress.init(this, R.color.build_primary) }

        parent.getChildAt(0)
            .findViewById<LinearLayout>(aytR.id.youtube_player_seekbar)
            ?.children
            ?.filter { it is SeekBar }
            ?.first()
            ?.let { it as SeekBar }
            ?.isEnabled = false
    }

    fun switchView() {
        if (!_isSwitching) {
            (_playerView?.parent as FrameLayout?)?.removeView(_playerView)
            _mviView = null
            _fadeViewHelper?.apply { _player?.removeListener(this) }
            _isSwitching = true
        }
    }

    fun willFinish() = !_isSwitching

    fun cleanupIfNotSwitching() {
        if (!_isSwitching) {
            log.d("cleanup player")
            _playerView?.release()
            _playerView = null
            _mviView = null
            _player = null
            _progressBar = null
            _fadeViewHelper?.apply { _player?.removeListener(this) }
            _currentVideoId = null
            _lastPositionSec = -1f
            _lastPositionSend = -1f
        }
        _isSwitching = false
    }

    private val ytPlayerListener = object : YouTubePlayerListener {
        override fun onReady(youTubePlayer: YouTubePlayer) {
            _player = youTubePlayer
            log.d("onReady")
            _mviView?.dispatch(Event.PlayerStateChanged(VIDEO_CUED))
            controlsView?.apply { addFadeControlsFor(this) }
        }

        override fun onApiChange(youTubePlayer: YouTubePlayer) = Unit

        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
            _player = youTubePlayer
            _lastPositionSec = second
            if (abs(_lastPositionSend - second) > 1) {
                _lastPositionSend = second
                _mviView?.dispatch(Event.PositionReceived((second * 1000).toLong()))
            }
        }

        override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
            _player = youTubePlayer
            _mviView?.dispatch(Event.PlayerStateChanged(ERROR))
        }

        override fun onPlaybackQualityChange(
            youTubePlayer: YouTubePlayer,
            playbackQuality: PlayerConstants.PlaybackQuality
        ) {
            _player = youTubePlayer
        }

        override fun onPlaybackRateChange(
            youTubePlayer: YouTubePlayer,
            playbackRate: PlayerConstants.PlaybackRate
        ) {
            _player = youTubePlayer
        }

        override fun onStateChange(
            youTubePlayer: YouTubePlayer,
            state: PlayerConstants.PlayerState
        ) {
            _player = youTubePlayer
            playerState = when (state) {
                PlayerConstants.PlayerState.ENDED -> ENDED
                PlayerConstants.PlayerState.PAUSED -> PAUSED
                PlayerConstants.PlayerState.PLAYING -> PLAYING
                PlayerConstants.PlayerState.BUFFERING -> BUFFERING
                PlayerConstants.PlayerState.UNSTARTED -> UNSTARTED
                PlayerConstants.PlayerState.UNKNOWN -> UNKNOWN
                PlayerConstants.PlayerState.VIDEO_CUED -> VIDEO_CUED
            }
            _mviView?.dispatch(Event.PlayerStateChanged(playerState))
            _progressBar?.isVisible =
                (playerState == UNKNOWN || playerState == VIDEO_CUED ||
                        playerState == BUFFERING || playerState == UNSTARTED)

            if (playerState == PAUSED) _fadeViewHelper?.toggleVisibility()
        }

        override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
            _player = youTubePlayer
            _mviView?.dispatch(Event.DurationReceived((duration * 1000).toLong()))
        }

        override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
            _player = youTubePlayer
            _currentVideoId = videoId
            _mviView?.dispatch(Event.IdReceived(videoId))
        }

        override fun onVideoLoadedFraction(
            youTubePlayer: YouTubePlayer,
            loadedFraction: Float
        ) {
            _player = youTubePlayer
        }

    }

    fun processCommand(command: PlayerContract.PlayerCommand) {
        log.d(command.toString())
        when (command) {
            is PlayerContract.PlayerCommand.Load -> {
                log.d("PlayerCommand.Load: $_currentVideoId != ${command.item.media.platformId} start:${command.startPosition}")
                if (_currentVideoId != command.item.media.platformId) {
                    _player?.loadVideo(command.item.media.platformId, command.startPosition / 1000f)
                    _progressBar?.isVisible = true
                } else {
                    _player?.play()
                }
            }

            is PlayerContract.PlayerCommand.Play -> _player?.play()
            is PlayerContract.PlayerCommand.Pause -> _player?.pause()
            is PlayerContract.PlayerCommand.SkipBack -> _player?.seekTo(_lastPositionSec - command.ms / 1000f)
            is PlayerContract.PlayerCommand.SkipFwd -> _player?.seekTo(_lastPositionSec + command.ms / 1000f)
            is PlayerContract.PlayerCommand.SeekTo -> {
                log.d("seekTo:${command.ms}")
                _player?.seekTo(command.ms.toFloat() / 1000f)
            }

            else -> Unit
        }
    }

    private fun addFadeControlsFor(root: ViewGroup) {
        _fadeViewHelper = FadeViewHelper(root).apply {
            animationDuration = 300
            fadeOutDelay = 3000
            _player?.addListener(this)
                ?: throw IllegalStateException("Player not yet initialized")
        }
    }
}
