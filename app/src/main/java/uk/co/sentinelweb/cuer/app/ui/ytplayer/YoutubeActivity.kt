package uk.co.sentinelweb.cuer.app.ui.ytplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.databinding.ActivityYoutubeBinding
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ITEM
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.Command
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.player.PlayerModelMapper
import uk.co.sentinelweb.cuer.app.util.extension.activityLegacyScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise
import uk.co.sentinelweb.cuer.net.ApiKeyProvider
import uk.co.sentinelweb.cuer.net.retrofit.ServiceType

/**
 * YouTube standalone player (Landscape only)
 * Since YouTubeBaseActivity is older than time and still uses Activity then thee are no fragments
 * https://issuetracker.google.com/issues/35172585
 *
 * Other players exist which are used here - this is just one option for playback
 */
class YoutubeActivity : YouTubeBaseActivity(),
    YouTubePlayer.OnInitializedListener,
    AndroidScopeComponent {

    override val scope: Scope by activityLegacyScopeWithSource()

    private val toastWrapper: ToastWrapper by inject()
    private val apiKeyProvider: ApiKeyProvider by inject(named(ServiceType.YOUTUBE))
    private val controller: PlayerController by inject()
    private val log: LogWrapper by inject()
    private val coroutines: CoroutineContextProvider by inject()
    private val showHideUi: ShowHideUi by inject()

    lateinit var view: YouTubePlayerViewImpl
    private lateinit var binding: ActivityYoutubeBinding

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar?.setDisplayHomeAsUpEnabled(true)

        showHideUi.showElements = { binding.controls.root.isVisible = true }
        showHideUi.hideElements = { binding.controls.root.isVisible = false }
        binding.controls.controlsTrackNext.setOnClickListener { view.dispatch(TrackFwdClicked) }
        binding.controls.controlsTrackLast.setOnClickListener { view.dispatch(TrackBackClicked) }
        binding.controls.controlsSeekBack.setOnClickListener { view.dispatch(SkipBackClicked) }
        binding.controls.controlsSeekForward.setOnClickListener { view.dispatch(SkipFwdClicked) }
        binding.youtubeView.initialize(apiKeyProvider.key, this)

        binding.youtubeWrapper.listener = object : InterceptorFrameLayout.OnTouchInterceptListener {
            override fun touched() {
                showHideUi.toggle()
            }
        }
    }

    override fun onDestroy() {
        controller.onViewDestroyed()
        controller.onDestroy()
        scope.close()
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        controller.onStart()
    }

    override fun onStop() {
        super.onStop()
        controller.onStop()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        showHideUi.delayedHide(100)
    }

    inner class YouTubePlayerViewImpl constructor(
        private val player: YouTubePlayer
    ) : BaseMviView<Model, PlayerContract.View.Event>(),
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

                override fun onLoaded(p0: String?) {
                    dispatch(PlayerStateChanged(VIDEO_CUED))
                }
            })
            player.setPlaybackEventListener(object : YouTubePlayer.PlaybackEventListener {
                override fun onPlaying() = dispatch(PlayerStateChanged(PLAYING))

                override fun onPaused() = dispatch(PlayerStateChanged(PAUSED))

                override fun onStopped() = Unit //dispatch(PlayerStateChanged(ENDED))

                override fun onBuffering(isBuffering: Boolean) {
                    if (isBuffering)
                        dispatch(PlayerStateChanged(BUFFERING))
                    else
                        showHideUi.hide()
                        dispatch(if (player.isPlaying) PlayerStateChanged(PLAYING) else PlayerStateChanged(PAUSED))
                }

                override fun onSeekTo(targetPosition: Int) {
                    showHideUi.hide()
                    player.play()
                }

            })
        }

        override val renderer: ViewRenderer<Model> = diff {
            diff(get = Model::platformId, set = player::cueVideo)
            diff(get = Model::texts, set = { texts ->
                binding.controls.apply {
                    controlsVideoTitle.text = texts.title
                    controlsVideoPlaylist.text = texts.playlistTitle
                    controlsVideoPlaylistData.text = texts.playlistData
                    controlsTrackLastText.text = texts.lastTrackText
                    controlsTrackNextText.text = texts.nextTrackText
                }
            })
        }

        fun init() {
            log.d("view.init")
            player.setShowFullscreenButton(false)
            player.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL)
            dispatch(Initialised)
        }

        override suspend fun processLabel(l: PlayerContract.MviStore.Label) {
            when (l) {
                is Command -> l.command.let { command ->
                    when (command) {
                        is Play -> player.play()
                        is Pause -> player.pause()
                        is SkipBack -> player.seekToMillis(player.currentTimeMillis - command.ms)
                        is SkipFwd -> player.seekToMillis(player.currentTimeMillis + command.ms)
                        is JumpTo -> player.seekToMillis(command.ms)
                    }
                }
            }
        }

    }

    // region YouTubePlayer.OnInitializedListener
    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider, player: YouTubePlayer,
        wasRestored: Boolean
    ) {
        if (!wasRestored) {
            log.d("onInitializationSuccess")
            view = YouTubePlayerViewImpl(player)
            controller.onViewCreated(view)
            controller.onStart()
            coroutines.mainScope.launch {
                delay(300)
                view.init()
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

        fun start(c: Context, playlistItem: PlaylistItemDomain) = c.startActivity(
            Intent(c, YoutubeActivity::class.java).apply {
                putExtra(PLAYLIST_ITEM.toString(), playlistItem.serialise())
            })

        private const val RECOVERY_DIALOG_REQUEST = 1

        @JvmStatic
        val activityModule = module {
            scope(named<YoutubeActivity>()) {
                scoped { PlayerController(get(), LoggingStoreFactory(DefaultStoreFactory), get(), get(), get(), get()) }
                scoped<PlayerContract.PlaylistItemLoader> { ItemLoader(getSource(), get()) }
                scoped { ShowHideUi(getSource()) }
                scoped { PlayerModelMapper() }
            }

        }
    }

}
