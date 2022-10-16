package uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_land

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.ActivityAytFullsreenBinding
import uk.co.sentinelweb.cuer.app.databinding.FullscreenControlsOverlayBinding
import uk.co.sentinelweb.cuer.app.ui.common.dialog.support.SupportDialogFragment
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ITEM
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationRouter
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.ytplayer.AytViewHolder
import uk.co.sentinelweb.cuer.app.ui.ytplayer.LocalPlayerCastListener
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerServiceManager
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.extension.activityScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise


@ExperimentalCoroutinesApi
class AytLandActivity : AppCompatActivity(),
    AndroidScopeComponent {

    override val scope: Scope by activityScopeWithSource<AytLandActivity>()

    private val controller: PlayerController by inject()
    private val log: LogWrapper by inject()
    private val coroutines: CoroutineContextProvider by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val navRouter: NavigationRouter by inject()
    private val toast: ToastWrapper by inject()
    private val res: ResourceWrapper by inject()
    private val castListener: LocalPlayerCastListener by inject()
    private val chromeCastWrapper: ChromeCastWrapper by inject()
    private val floatingService: FloatingPlayerServiceManager by inject()
    private val aytViewHolder: AytViewHolder by inject()

    private lateinit var mviView: AytLandActivity.MviViewImpl
    private lateinit var binding: ActivityAytFullsreenBinding
    private lateinit var controlsBinding: FullscreenControlsOverlayBinding

    private var currentItem: PlaylistItemDomain? = null

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

    override fun onStart() {
        super.onStart()
        // this POS restores the ayt player to the mvi kotlin state after returning from a home press
        if (this::mviView.isInitialized) {
            coroutines.mainScope.launch {
                delay(50)
                mviView.dispatch(PlayerStateChanged(aytViewHolder.playerState))
            }
        }
    }

    override fun onDestroy() {
        castListener.release()
        controller.onViewDestroyed()
        controller.onDestroy(aytViewHolder.willFinish())
        aytViewHolder.cleanupIfNotSwitching()
        super.onDestroy()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (floatingService.isRunning()) {
            aytViewHolder.switchView()
            floatingService.stop()
        }
        mviView = MviViewImpl(aytViewHolder)
        aytViewHolder.playerView
            ?.apply { getLifecycle().addObserver(this) }
            ?: throw IllegalStateException("Player is not created")
        controller.onViewCreated(
            listOf(mviView),
            lifecycle.asEssentyLifecycle()
        )
        controlsBinding = FullscreenControlsOverlayBinding.bind(
            aytViewHolder.playerView!!
                .inflateCustomPlayerUi(R.layout.fullscreen_controls_overlay)
                .findViewById(R.id.controls_video_root)
        )
        aytViewHolder.controlsView = controlsBinding.root

        controlsBinding.controlsTrackNext.setOnClickListener {
            mviView.dispatch(TrackFwdClicked)
        }
        controlsBinding.controlsTrackLast.setOnClickListener {
            mviView.dispatch(TrackBackClicked)
        }
        controlsBinding.controlsSeekBack.setOnClickListener {
            mviView.dispatch(SkipBackClicked)
        }
        controlsBinding.controlsSeekForward.setOnClickListener {
            mviView.dispatch(SkipFwdClicked)
        }
        controlsBinding.controlsSeekBack.setOnLongClickListener { mviView.dispatch(SkipBackSelectClicked);true }
        controlsBinding.controlsSeekForward.setOnLongClickListener { mviView.dispatch(SkipFwdSelectClicked);true }
        controlsBinding.controlsPlayFab.setOnClickListener { mviView.dispatch(PlayPauseClicked()) }
//        controlsBinding.controlsPortraitFab.setOnClickListener { mviView.dispatch(PortraitClick); }
//        controlsBinding.controlsPipFab.setOnClickListener { mviView.dispatch(PipClick); }
        controlsBinding.controlsSupport.setOnClickListener { mviView.dispatch(Support); }
        controlsBinding.controlsClose.setOnClickListener { finish() }
        controlsBinding.controlsSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(view: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mviView.dispatch(SeekBarChanged(seekBar.progress / seekBar.max.toFloat()))
            }
        })
        chromeCastWrapper.initMediaRouteButton(controlsBinding.controlsMediaRouteButton)
        // defaults
        controlsBinding.controlsPlayFab.isVisible = false
        controlsBinding.controlsSeek.isVisible = false
    }

    // region MVI view
    inner class MviViewImpl(aytViewHolder: AytViewHolder) :
        BaseMviView<Model, Event>(),
        PlayerContract.View {

        init {
            aytViewHolder.addView(this@AytLandActivity, binding.playerContainer, this, true)
        }

        override val renderer: ViewRenderer<Model> = diff {
            diff(get = Model::playState, set = {
                when (it) {
                    BUFFERING -> controlsBinding.controlsPlayFab.showProgress(true)
                    PLAYING -> {
                        updatePlayingIcon(true)
                        controlsBinding.controlsPlayFab.showProgress(false)
                    }

                    PAUSED -> {
                        updatePlayingIcon(false)
                        controlsBinding.controlsPlayFab.showProgress(false)
                    }

                    else -> Unit
                }
            })
            diff(get = Model::playlistItem, set = {
                currentItem = it
            })
            diff(get = Model::texts, set = { texts ->
                controlsBinding.apply {
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
                controlsBinding.apply {
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
                    aytViewHolder.processCommand(command)
                }
                is LinkOpen ->
                    navRouter.navigate(NavigationModel(WEB_LINK, mapOf(LINK to label.url)))
                is ChannelOpen ->
                    label.channel.platformId?.let { id -> navRouter.navigate(NavigationModel(YOUTUBE_CHANNEL, mapOf(CHANNEL_ID to id))) }
                is FullScreenPlayerOpen -> toast.show("Already in protrait mode - shouldnt get here")
                is PipPlayerOpen -> {
                    val hasPermission = floatingService.hasPermission(this@AytLandActivity)
                    if (hasPermission) {
                        aytViewHolder.switchView()
                    }
                    floatingService.start(this@AytLandActivity, label.item)
                    if (hasPermission) {
                        finishAffinity()
                    }
                }
                is PortraitPlayerOpen -> label.also {
                    aytViewHolder.switchView()
                    navRouter.navigate(NavigationModel(LOCAL_PLAYER, mapOf(PLAYLIST_ITEM to it.item)))
                    finish()
                }
                is ShowSupport -> SupportDialogFragment.show(this@AytLandActivity, label.item.media)
            }
        }

        fun updatePlayingIcon(isPlaying: Boolean) {
            if (isPlaying) {
                controlsBinding.controlsPlayFab.setImageState(
                    intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked),
                    false
                )
            } else {
                controlsBinding.controlsPlayFab.setImageState(intArrayOf(android.R.attr.state_enabled), false)
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