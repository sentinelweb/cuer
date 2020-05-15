package uk.co.sentinelweb.cuer.app.ui.ytplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import kotlinx.android.synthetic.main.activity_youtube.*
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.NavigateParam.MEDIA_ID
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.net.youtube.YoutubeApiKeyProvider

/**
 * TODO clean up (a lot)
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class YoutubeActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener {

    private val toastWrapper: ToastWrapper by inject()
    private val apiKeyProvider: YoutubeApiKeyProvider by inject()

    // jesus fin christ ..
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

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
    }

    //
    private fun hide() {
        // Hide UI first
        actionBar?.hide()
        //fullscreen_content_controls.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    //
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
            intent?.getStringExtra(MEDIA_ID.toString())?.let {
                player.cueVideo(it)
                player.setShowFullscreenButton(false)

                player.setPlayerStateChangeListener(object :
                    YouTubePlayer.PlayerStateChangeListener {
                    override fun onVideoEnded() {
                        show()
                    }

                    override fun onVideoStarted() {
                        hide()
                    }

                    override fun onError(p0: YouTubePlayer.ErrorReason?) = Unit

                    override fun onAdStarted() = Unit

                    override fun onLoading() = Unit

                    override fun onLoaded(p0: String?) {
                        player.play()
                    }
                })
            }
        }
    }

    override fun onInitializationFailure(
        provider: YouTubePlayer.Provider,
        errorReason: YouTubeInitializationResult
    ) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(
                this,
                RECOVERY_DIALOG_REQUEST
            ).show()
        } else {
            toastWrapper.show("Could init Youtube player $errorReason")
        }
    }
    // endregion

    companion object {
        // todo map in NavigationMapper
        fun start(c: Context, mediaId: String) = c.startActivity(
            Intent(c, YoutubeActivity::class.java).apply {
                mediaId.let { putExtra(MEDIA_ID.toString(), it) }
            })

        private const val RECOVERY_DIALOG_REQUEST = 1

        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300

        @JvmStatic
        val activityModule = module {
            scope(named<YoutubeActivity>()) {
//                    scoped<PlayerContract.View> { getSource() }
//                    scoped<PlayerContract.Presenter> { PlayerPresenter(get(), get(), get()) }
//                    scoped { PlayerModelMapper() }
//                    viewModel { PlayerState() }
            }

        }
    }
}
