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
import kotlinx.android.synthetic.main.activity_youtube.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ITEM
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
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
 * TODO clean up (a lot)
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
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

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_youtube)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        showHideUi.showElements = { controls.isVisible = true }
        showHideUi.hideElements = { controls.isVisible = false }
        youtube_track_next.setOnClickListener { view.dispatch(TrackFwdClicked) }
        youtube_track_last.setOnClickListener { view.dispatch(TrackBackClicked) }
        youtube_seek_back.setOnClickListener { view.dispatch(SkipBackClicked) }
        youtube_seek_forward.setOnClickListener { view.dispatch(SkipFwdClicked) }
        youtube_view.initialize(apiKeyProvider.key, this)

        youtube_wrapper.listener = object : InterceptorFrameLayout.OnTouchInterceptListener {
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
                        dispatch(if (player.isPlaying) PlayerStateChanged(PLAYING) else PlayerStateChanged(PAUSED))
                }

                override fun onSeekTo(targetPosition: Int) {
                    showHideUi.hide()
                    player.play()
                    //dispatch(PlayerStateChanged(BUFFERING))
                }

            })
        }

        override val renderer: ViewRenderer<Model> = diff {
            diff(get = Model::platformId, set = player::cueVideo)
            diff(get = Model::title, set = { actionBar?.setTitle(it) })
            diff(get = Model::playCommand, set = {
                when (it) {
                    is Play -> player.play()
                    is Pause -> player.pause()
                    is SkipFwd -> player.seekToMillis(player.currentTimeMillis + it.ms)
                    is SkipBack -> player.seekToMillis(player.currentTimeMillis - it.ms)
                    is JumpTo -> player.seekToMillis(it.ms)
                }
            })
        }

        fun init() {
            log.d("view.init")
            player.setShowFullscreenButton(false)
            dispatch(Initialised)
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
                scoped { PlayerController(get(), LoggingStoreFactory(DefaultStoreFactory), get(), get(), get()) }
                scoped<PlayerContract.PlaylistItemLoader> { ItemLoader(getSource(), get()) }
                scoped { ShowHideUi(getSource()) }
            }

        }
    }

}
