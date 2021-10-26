package uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_land

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.arkivanov.mvikotlin.core.lifecycle.asMviLifecycle
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.ActivityAytFullsreenBinding
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.ytplayer.InterceptorFrameLayout
import uk.co.sentinelweb.cuer.app.ui.ytplayer.LocalPlayerCastListener
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ShowHideUi
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.extension.activityScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.view.fadeIn
import uk.co.sentinelweb.cuer.app.util.extension.view.fadeOut
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise
import kotlin.math.abs

class AytLandActivity : AppCompatActivity(),
    AndroidScopeComponent {

    override val scope: Scope by activityScopeWithSource()

    private val controller: PlayerController by inject()
    private val log: LogWrapper by inject()
    private val coroutines: CoroutineContextProvider by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val navMapper: NavigationMapper by inject()
    private val toast: ToastWrapper by inject()
    private val showHideUi: ShowHideUi by inject()
    private val res: ResourceWrapper by inject()
    private val castListener: LocalPlayerCastListener by inject()
    private val chromeCastWrapper: ChromeCastWrapper by inject()

    private lateinit var mviView: AytLandActivity.MviViewImpl
    private lateinit var binding: ActivityAytFullsreenBinding

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAytFullsreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        edgeToEdgeWrapper.setDecorFitsSystemWindows(this)
        castListener.listen()
    }

    override fun onDestroy() {
        castListener.release()
        super.onDestroy()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mviView = MviViewImpl(binding.fullscreenPlayerVideo)
        getLifecycle().addObserver(binding.fullscreenPlayerVideo)
        controller.onViewCreated(listOf(mviView), lifecycle.asMviLifecycle())
        showHideUi.showElements = {
            log.d("showElements")
            binding.controls.root.fadeIn()
            binding.controls.root.requestFocus()
        }
        showHideUi.hideElements = {
            log.d("hideElements")
            binding.controls.root.fadeOut()
        }
        binding.controls.controlsTrackNext.setOnClickListener { mviView.dispatch(TrackFwdClicked);showHideUi.delayedHide() }
        binding.controls.controlsTrackLast.setOnClickListener { mviView.dispatch(TrackBackClicked);showHideUi.delayedHide() }
        binding.controls.controlsSeekBack.setOnClickListener {
            mviView.dispatch(SkipBackClicked);
            showHideUi.delayedHide()
        }
        binding.controls.controlsSeekForward.setOnClickListener {
            mviView.dispatch(SkipFwdClicked);
            showHideUi.delayedHide()
        }
        binding.controls.controlsSeekBack.setOnLongClickListener { mviView.dispatch(SkipBackSelectClicked);true }
        binding.controls.controlsSeekForward.setOnLongClickListener { mviView.dispatch(SkipFwdSelectClicked);true }
        binding.controls.controlsPlayFab.setOnClickListener { mviView.dispatch(PlayPauseClicked()) }
        binding.controls.controlsPortraitFab.setOnClickListener { mviView.dispatch(PortraitClick); }
        binding.controls.controlsPipFab.setOnClickListener { mviView.dispatch(PipClick); }
        binding.controls.controlsSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    showHideUi.delayedHide()
                }
            }

            override fun onStartTrackingTouch(view: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mviView.dispatch(SeekBarChanged(seekBar.progress / seekBar.max.toFloat()));showHideUi.delayedHide()
            }
        })


        binding.fullscreenVideoWrapper.listener = object : InterceptorFrameLayout.OnTouchInterceptListener {
            override fun touched() {
                log.d("fullscreenVideoWrapper -  touched visible:${binding.controls.root.isVisible}")
                if (!binding.controls.root.isVisible) {
                    showHideUi.showUiIfNotVisible()
                }
            }
        }
        binding.controls.root.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                log.d("binding.controls.root.onTouch")
                return false
            }

        })

        chromeCastWrapper.initMediaRouteButton(binding.controls.controlsMediaRouteButton)
        // defaults
        showHideUi.hide()
        binding.controls.controlsPlayFab.isVisible = false
        binding.controls.controlsSeek.isVisible = false
    }

    // region MVI view
    inner class MviViewImpl(playerView: YouTubePlayerView) :
        BaseMviView<Model, Event>(),
        PlayerContract.View {
        private var player: YouTubePlayer? = null
        private var lastPositionSec: Float = -1f
        private var lastPositionSend: Float = -1f

        init {
            playerView.addYouTubePlayerListener(object : YouTubePlayerListener {
                override fun onApiChange(youTubePlayer: YouTubePlayer) = Unit

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    player = youTubePlayer
                    lastPositionSec = second
                    if (abs(lastPositionSend - second) > 1) {
                        lastPositionSend = second
                        dispatch(PositionReceived((second * 1000).toLong()))
                    }
                }

                override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                    player = youTubePlayer
                    dispatch(PlayerStateChanged(ERROR))
                }

                override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, playbackQuality: PlayerConstants.PlaybackQuality) {
                    player = youTubePlayer
                }

                override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: PlayerConstants.PlaybackRate) {
                    player = youTubePlayer
                }

                override fun onReady(youTubePlayer: YouTubePlayer) {
                    player = youTubePlayer
                    log.d("onReady")
                    dispatch(PlayerStateChanged(VIDEO_CUED))
                }

                override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                    player = youTubePlayer
                    val playStateDomain = when (state) {
                        PlayerConstants.PlayerState.ENDED -> ENDED
                        PlayerConstants.PlayerState.PAUSED -> PAUSED
                        PlayerConstants.PlayerState.PLAYING -> PLAYING
                        PlayerConstants.PlayerState.BUFFERING -> BUFFERING
                        PlayerConstants.PlayerState.UNSTARTED -> UNSTARTED
                        PlayerConstants.PlayerState.UNKNOWN -> UNKNOWN
                        PlayerConstants.PlayerState.VIDEO_CUED -> VIDEO_CUED
                    }
                    dispatch(PlayerStateChanged(playStateDomain))
                }

                override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                    player = youTubePlayer
                    dispatch(DurationReceived((duration * 1000).toLong()))
                }

                override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
                    player = youTubePlayer
                    dispatch(IdReceived(videoId))
                    //dispatch(PlayerStateChanged(VIDEO_CUED))
                }

                override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {
                    player = youTubePlayer
                }

            })

        }

        override val renderer: ViewRenderer<Model> = diff {
//            diff(get = Model::description, set = {
//                log.d("set description")
//                binding.portraitPlayerDescription.setModel(it)
//            })
//            diff(get = Model::screen, set = {
//                when (it) {
//                    DESCRIPTION -> {
//                        binding.portraitPlayerDescription.isVisible = true
//                        binding.portraitPlayerPlaylist.isVisible = false
//                    }
//                    PLAYLIST -> {
//                        binding.portraitPlayerDescription.isVisible = false
//                        binding.portraitPlayerPlaylist.isVisible = true
//                    }
//                }
//            })
            diff(get = Model::playState, set = {
                when (it) {
                    BUFFERING -> binding.controls.controlsPlayFab.showProgress(true)
                    PLAYING -> {
                        updatePlayingIcon(true)
                        binding.controls.controlsPlayFab.showProgress(false)
                    }
                    PAUSED -> {
                        updatePlayingIcon(false)
                        binding.controls.controlsPlayFab.showProgress(false)
                    }
                }
            })
            diff(get = Model::texts, set = { texts ->
                binding.controls.apply {
                    controlsVideoTitle.text = texts.title
                    controlsVideoPlaylist.text = texts.playlistTitle
                    controlsVideoPlaylistData.text = texts.playlistData
                    controlsTrackLastText.text = texts.lastTrackText
                    controlsTrackLastText.isVisible = texts.lastTrackText != ""
                    controlsTrackNextText.text = texts.nextTrackText
                    controlsTrackNextText.isVisible = texts.nextTrackText != ""
                    controlsSkipfwdText.text = texts.skipFwdText
                    controlsSkipbackText.text = texts.skipBackText
                }
            })
            diff(get = Model::times, set = { times ->
                binding.controls.apply {
                    controlsSeek.progress = (times.seekBarFraction * controlsSeek.max).toInt()
                    controlsCurrentTime.text = times.positionText

                    if (times.isLive) {
                        controlsDuration.setBackgroundColor(res.getColor(R.color.live_background))
                        controlsDuration.text = res.getString(R.string.live)
                        controlsSeek.isEnabled = false
                    } else {
                        controlsDuration.setBackgroundColor(res.getColor(R.color.info_text_overlay_background))
                        controlsDuration.text = times.durationText
                        controlsSeek.isEnabled = true
                    }
                }
            })
        }

        override suspend fun processLabel(label: PlayerContract.MviStore.Label) {
            when (label) {
                is Command -> label.command.let { command ->
                    log.d(command.toString())
                    when (command) {
                        is Load -> player?.loadVideo(command.platformId, command.startPosition / 1000f)
                        is Play -> player?.play()
                        is Pause -> player?.pause()
                        is SkipBack -> player?.seekTo(lastPositionSec - command.ms / 1000f)
                        is SkipFwd -> player?.seekTo(lastPositionSec + command.ms / 1000f)
                        is SeekTo -> {
                            log.d(command.toString())
                            player?.seekTo(command.ms.toFloat() / 1000f)
                        }
                        else -> Unit
                    }
                }
                is LinkOpen ->
                    navMapper.navigate(NavigationModel(WEB_LINK, mapOf(LINK to label.url)))
                is ChannelOpen ->
                    label.channel.platformId?.let { id -> navMapper.navigate(NavigationModel(YOUTUBE_CHANNEL, mapOf(CHANNEL_ID to id))) }
                is FullScreenPlayerOpen -> toast.show("Already in protrait mode - shouldnt get here")
                is PipPlayerOpen -> toast.show("PIP Open")
                is PortraitPlayerOpen -> label.also {
                    navMapper.navigate(NavigationModel(LOCAL_PLAYER, mapOf(PLAYLIST_ITEM to it.item)))
                    finish()
                }
            }
        }

        fun updatePlayingIcon(isPlaying: Boolean) {
            if (isPlaying) {
                binding.controls.controlsPlayFab.setImageState(intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked),
                    false)
            } else {
                binding.controls.controlsPlayFab.setImageState(intArrayOf(android.R.attr.state_enabled), false)
            }
        }
    }


    companion object {

        fun start(c: Context, playlistItem: PlaylistItemDomain) = c.startActivity(
            Intent(c, AytLandActivity::class.java).apply {
                putExtra(PLAYLIST_ITEM.toString(), playlistItem.serialise())
            })
    }


}