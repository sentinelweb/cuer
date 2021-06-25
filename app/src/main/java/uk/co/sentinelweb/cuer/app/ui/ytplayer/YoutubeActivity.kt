package uk.co.sentinelweb.cuer.app.ui.ytplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.PAUSE
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.PLAY
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.PlayerStateChanged
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.TrackFwdClicked
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

    lateinit var view: YouTubePlayerViewImpl

    init {
        log.tag(this)
    }

    // jesus fin christ .. todo conver all runnables to coroutines
    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        youtube_view.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        actionBar?.show()
        //fullscreen_content_controls.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    @SuppressLint("ClickableViewAccessibility")
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_youtube)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        mVisible = true

        // Set up the user interaction to manually show or hide the system UI.
        youtube_view.initialize(apiKeyProvider.key, this)

        youtube_wrapper.listener = object : InterceptorFrameLayout.OnTouchInterceptListener {
            override fun touched() {
                toggle()
            }
        }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        dummy_button.setOnTouchListener(mDelayHideTouchListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.local_player_actionbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.next -> view.dispatch(TrackFwdClicked)
            R.id.previous -> view.dispatch(PlayerContract.View.Event.TrackBackClicked)
        }
        return super.onOptionsItemSelected(item)
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

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    inner class YouTubePlayerViewImpl constructor(
        private val player: YouTubePlayer
    ) : BaseMviView<Model, PlayerContract.View.Event>(),
        PlayerContract.View {
        init {
            player.setPlayerStateChangeListener(object : YouTubePlayer.PlayerStateChangeListener {
                override fun onVideoEnded() {
                    show()
                    dispatch(PlayerStateChanged(ENDED))
                }

                override fun onVideoStarted() {
                    hide()
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

                override fun onBuffering(p0: Boolean) = dispatch(PlayerStateChanged(BUFFERING))

                override fun onSeekTo(p0: Int) = Unit //dispatch(PlayerStateChanged(BUFFERING))

            })
        }

        override val renderer: ViewRenderer<Model> = diff {
            diff(get = Model::platformId, set = player::cueVideo)
            diff(get = Model::title, set = { actionBar?.setTitle(it) })
            diff(get = Model::playCommand, set = {
                when (it) {
                    PLAY -> player.play()
                    PAUSE -> player.pause()
                }
            })
        }

        fun init() {
            log.d("view.init")
            player.setShowFullscreenButton(false)
            dispatch(PlayerContract.View.Event.Initialised)
        }

    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
    }

    private fun hide() {
        // Hide UI first
        actionBar?.hide()
        //fullscreen_content_controls.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        youtube_view.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }
//
    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
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
            toastWrapper.show("Could init Youtube player $errorReason")
        }
    }
    // endregion

    companion object {

        fun start(c: Context, playlistItem: PlaylistItemDomain) = c.startActivity(
            Intent(c, YoutubeActivity::class.java).apply {
                putExtra(PLAYLIST_ITEM.toString(), playlistItem.serialise())
            })

        private const val RECOVERY_DIALOG_REQUEST = 1
        private val AUTO_HIDE = true
        private val AUTO_HIDE_DELAY_MILLIS = 3000
        private val UI_ANIMATION_DELAY = 300

        @JvmStatic
        val activityModule = module {
            scope(named<YoutubeActivity>()) {
                scoped { PlayerController(get(), LoggingStoreFactory(DefaultStoreFactory), get(), get(), get()) }
                scoped<PlayerContract.PlaylistItemLoader> { ItemLoader(getSource(), get()) }
            }

        }
    }

}
