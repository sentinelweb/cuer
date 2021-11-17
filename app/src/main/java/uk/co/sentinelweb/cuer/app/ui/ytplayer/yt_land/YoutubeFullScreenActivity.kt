package uk.co.sentinelweb.cuer.app.ui.ytplayer.yt_land


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import androidx.core.view.isVisible
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.ActivityYoutubeFullsreenBinding
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ITEM
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.ytplayer.InterceptorFrameLayout
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ShowHideUi
import uk.co.sentinelweb.cuer.app.util.extension.activityLegacyScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.ext.tickerFlow
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise
import uk.co.sentinelweb.cuer.net.ApiKeyProvider
import uk.co.sentinelweb.cuer.net.retrofit.ServiceType
import kotlin.time.ExperimentalTime
import android.R as RA

/**
 * YouTube standalone player (Landscape only)
 * Since YouTubeBaseActivity is older than time and still uses Activity then thee are no fragments
 * https://issuetracker.google.com/issues/35172585
 *
 * Other players exist which are used here - this is just one option for playback
 */
@ExperimentalTime
class YoutubeFullScreenActivity : YouTubeBaseActivity(),
    YouTubePlayer.OnInitializedListener,
    AndroidScopeComponent {

    override val scope: Scope by activityLegacyScopeWithSource()

    private val toastWrapper: ToastWrapper by inject()
    private val apiKeyProvider: ApiKeyProvider by inject(named(ServiceType.YOUTUBE))
    private val controller: PlayerController by inject()
    private val log: LogWrapper by inject()
    private val coroutines: CoroutineContextProvider by inject()
    private val showHideUi: ShowHideUi by inject()
    private val res: ResourceWrapper by inject()
    private val navMapper: NavigationMapper by inject()
    private val toast: ToastWrapper by inject()

    lateinit var mviView: YouTubePlayerViewImpl
    private lateinit var binding: ActivityYoutubeFullsreenBinding

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubeFullsreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar?.setDisplayHomeAsUpEnabled(true)

        showHideUi.showElements = {
            binding.controls.root.isVisible = true
            if (this::mviView.isInitialized) {
                mviView.updatePlayingIcon()
            }
        }
        showHideUi.hideElements = { binding.controls.root.isVisible = false }
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
        binding.controls.controlsPlayFab.setOnClickListener { mviView.playPause() }
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
        binding.youtubeView.initialize(apiKeyProvider.key, this)

        binding.youtubeWrapper.listener = object : InterceptorFrameLayout.OnTouchInterceptListener {
            override fun touched() {

                if (!binding.controls.root.isVisible) {
                    showHideUi.showUiIfNotVisible()
                }
            }
        }

    }

    override fun onDestroy() {
        mviView.release()
        controller.onViewDestroyed()
        controller.onDestroy(true)
        scope.close()
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        controller.onStart()
        if (this::mviView.isInitialized) {
            mviView.onStart()
        }
    }

    override fun onStop() {
        super.onStop()
        mviView.onStop()
        controller.onStop()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        showHideUi.delayedHide(100)
    }

    // region MVI view
    inner class YouTubePlayerViewImpl constructor(
        private val player: YouTubePlayer
    ) : BaseMviView<Model, Event>(),
        PlayerContract.View {
        init {
            player.setPlayerStateChangeListener(object : YouTubePlayer.PlayerStateChangeListener {
                override fun onVideoEnded() {
                    showHideUi.show()
                    dispatch(PlayerStateChanged(ENDED))
                }

                override fun onVideoStarted() {
                    showHideUi.hide()
                }

                override fun onError(p0: YouTubePlayer.ErrorReason?) = dispatch(PlayerStateChanged(ERROR))

                override fun onAdStarted() = Unit

                override fun onLoading() = dispatch(PlayerStateChanged(BUFFERING))
                // button id name skip_ad_button (FrameLayout)

                override fun onLoaded(p0: String?) {
                    //dispatch(PlayerStateChanged(VIDEO_CUED))
                    player.play()
                }
            })
            player.setPlaybackEventListener(object : YouTubePlayer.PlaybackEventListener {
                override fun onPlaying() = dispatch(PlayerStateChanged(PLAYING))

                override fun onPaused() = dispatch(PlayerStateChanged(PAUSED))

                override fun onStopped() = Unit

                override fun onBuffering(isBuffering: Boolean) {
                    if (isBuffering)
                        dispatch(PlayerStateChanged(BUFFERING))
                    else
                        showHideUi.hide()
                    //dispatch(if (player.isPlaying) PlayerStateChanged(PLAYING) else PlayerStateChanged(PAUSED))
                    if (!player.isPlaying) player.play() else dispatch(PlayerStateChanged(PLAYING))
                }

                override fun onSeekTo(targetPosition: Int) {
                    showHideUi.hide()
                    player.play()
                }

            })
        }

        var ticker: Job? = null

        private fun startTicker() {
            ticker = tickerFlow(1000)
                .filter { player.isPlaying }
                .onEach { dispatch(PositionReceived(player.currentTimeMillis.toLong())) }
                .launchIn(coroutines.mainScope)
        }

        override val renderer: ViewRenderer<Model> = diff {
            //diff(get = Model::platformId, set = player::cueVideo)
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

        fun updatePlayingIcon(isPlaying: Boolean = player.isPlaying) {
            if (isPlaying) {
                binding.controls.controlsPlayFab.setImageState(intArrayOf(RA.attr.state_enabled, RA.attr.state_checked), false)
            } else {
                binding.controls.controlsPlayFab.setImageState(intArrayOf(RA.attr.state_enabled), false)
            }
        }

        override suspend fun processLabel(label: PlayerContract.MviStore.Label) {
            when (label) {
                is Command -> label.command.let { command ->
                    when (command) {
                        is Load -> player.cueVideo(command.platformId, command.startPosition.toInt())
                        is Play -> player.play()
                        is Pause -> player.pause()
                        is SkipBack -> player.seekToMillis(player.currentTimeMillis - command.ms)
                        is SkipFwd -> player.seekToMillis(player.currentTimeMillis + command.ms)
                        is SeekTo -> player.seekToMillis(command.ms.toInt())
                    }
                }
                is FullScreenPlayerOpen -> toast.show("Already in Fullscreen mode - shouldnt get here")
                is PipPlayerOpen -> toast.show("PIP Open")
                is PortraitPlayerOpen -> label.also {
                    navMapper.navigate(NavigationModel(NavigationModel.Target.LOCAL_PLAYER, mapOf(PLAYLIST_ITEM to it.item)))
                    finish()
                }
            }
        }

        fun init() {
            log.d("view.init")
            player.setShowFullscreenButton(false)
            //player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS)
            useYtOverlay()
            //dispatch(Initialised)
        }

        fun onStart() {
            startTicker()
        }

        fun onStop() {
            player.pause()
            ticker?.cancel()
        }

        fun release() {
            player.release()
        }

        fun playPause() {
            mviView.dispatch(PlayPauseClicked(player.isPlaying))
        }

        fun useYtOverlay() {
            binding.controls.apply {
                controlsPlayFab.isVisible = false
                controlsSeek.isVisible = false
                controlsVideoTitle.isVisible = false
                controlsDuration.isVisible = false
                controlsCurrentTime.isVisible = false
            }
        }

    }
    // endregion

    // region YouTubePlayer.OnInitializedListener
    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider, player: YouTubePlayer,
        wasRestored: Boolean
    ) {
        if (!wasRestored) {
            log.d("onInitializationSuccess")
            mviView = YouTubePlayerViewImpl(player)
            controller.onViewCreated(listOf(mviView))
            controller.onStart()
            mviView.onStart()
            coroutines.mainScope.launch {
                delay(300)
                mviView.init()
            }
        }
    }

    override fun onInitializationFailure(
        provider: YouTubePlayer.Provider,
        errorReason: YouTubeInitializationResult
    ) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show()
        } else {
            toastWrapper.show("Couldn't init Youtube player $errorReason")
        }
    }
    // endregion

    companion object {
        private const val RECOVERY_DIALOG_REQUEST = 1

        fun start(c: Context, playlistItem: PlaylistItemDomain) = c.startActivity(
            Intent(c, YoutubeFullScreenActivity::class.java).apply {
                putExtra(PLAYLIST_ITEM.toString(), playlistItem.serialise())
            })
    }

}
