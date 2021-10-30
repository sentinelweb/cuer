package uk.co.sentinelweb.cuer.app.ui.ytplayer

import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import kotlin.math.abs

class AytViewHolder(
    private val log: LogWrapper,
) {
    init {
        log.tag(this)
    }

    private var _playerView: YouTubePlayerView? = null
    private var _mviView: BaseMviView<Model, Event>? = null

    private var _player: YouTubePlayer? = null
    private var _lastPositionSec: Float = -1f
    private var _lastPositionSend: Float = -1f
    private var _currentVideoId: String? = null

    private var _isSwitching: Boolean = false

    fun create(activity: AppCompatActivity) {
        _playerView = LayoutInflater.from(activity).inflate(R.layout.view_ayt_video, null) as YouTubePlayerView
        addPlayerListener()
    }

    fun addView(activity: AppCompatActivity, parent: FrameLayout, mviView: BaseMviView<Model, Event>) {
        if (_playerView == null) create(activity)
        _mviView = mviView
        parent.addView(_playerView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    fun switchView() {
        if (!_isSwitching) {
            (_playerView?.parent as FrameLayout?)?.removeView(_playerView)
            _mviView = null
            _isSwitching = true
        }
    }

    fun cleanupIfNotSwitching() {
        if (!_isSwitching) {
            log.d("cleanup player")
            _playerView?.release()
            _playerView = null
            _mviView = null
            _player = null
            _currentVideoId = null
            _lastPositionSec = -1f
            _lastPositionSend = -1f
        }
        _isSwitching = false
    }

    private fun addPlayerListener() {
        _playerView?.addYouTubePlayerListener(object : YouTubePlayerListener {
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
                _mviView?.dispatch(Event.PlayerStateChanged(PlayerStateDomain.ERROR))
            }

            override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, playbackQuality: PlayerConstants.PlaybackQuality) {
                _player = youTubePlayer
            }

            override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: PlayerConstants.PlaybackRate) {
                _player = youTubePlayer
            }

            override fun onReady(youTubePlayer: YouTubePlayer) {
                _player = youTubePlayer
                log.d("onReady")
                _mviView?.dispatch(Event.PlayerStateChanged(PlayerStateDomain.VIDEO_CUED))
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                _player = youTubePlayer
                val playStateDomain = when (state) {
                    PlayerConstants.PlayerState.ENDED -> PlayerStateDomain.ENDED
                    PlayerConstants.PlayerState.PAUSED -> PlayerStateDomain.PAUSED
                    PlayerConstants.PlayerState.PLAYING -> PlayerStateDomain.PLAYING
                    PlayerConstants.PlayerState.BUFFERING -> PlayerStateDomain.BUFFERING
                    PlayerConstants.PlayerState.UNSTARTED -> PlayerStateDomain.UNSTARTED
                    PlayerConstants.PlayerState.UNKNOWN -> PlayerStateDomain.UNKNOWN
                    PlayerConstants.PlayerState.VIDEO_CUED -> PlayerStateDomain.VIDEO_CUED
                }
                _mviView?.dispatch(Event.PlayerStateChanged(playStateDomain))
                //updateMediaSessionManagerPlaybackState()// todo ??
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                _player = youTubePlayer
                _mviView?.dispatch(Event.DurationReceived((duration * 1000).toLong()))
            }

            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
                _player = youTubePlayer
                _currentVideoId = videoId
                _mviView?.dispatch(Event.IdReceived(videoId))
                //dispatch(PlayerStateChanged(VIDEO_CUED))
            }

            override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {
                _player = youTubePlayer
            }

        })
    }

    fun processCommand(command: PlayerContract.PlayerCommand) {
        log.d(command.toString())
        when (command) {
            is PlayerContract.PlayerCommand.Load -> {
                log.d("PlayerCommand.Load: $_currentVideoId != ${command.platformId}")
                if (_currentVideoId != command.platformId) {
                    _player?.loadVideo(command.platformId, command.startPosition / 1000f)
                } else {
                    _player?.play()
                }
            }
            is PlayerContract.PlayerCommand.Play -> _player?.play()
            is PlayerContract.PlayerCommand.Pause -> _player?.pause()
            is PlayerContract.PlayerCommand.SkipBack -> _player?.seekTo(_lastPositionSec - command.ms / 1000f)
            is PlayerContract.PlayerCommand.SkipFwd -> _player?.seekTo(_lastPositionSec + command.ms / 1000f)
            is PlayerContract.PlayerCommand.SeekTo -> _player?.seekTo(command.ms.toFloat() / 1000f)

            else -> Unit
        }
    }
}