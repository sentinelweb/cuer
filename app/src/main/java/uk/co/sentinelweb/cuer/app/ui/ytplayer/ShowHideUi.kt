package uk.co.sentinelweb.cuer.app.ui.ytplayer

import android.app.Activity
import android.os.Handler
import android.view.View

// todo convert all runnables to coroutines
class ShowHideUi constructor(private val a: Activity) {
    var showElements: () -> Unit = {}
    var hideElements: () -> Unit = {}
    private val mHideHandler = Handler()
    private val hideStatusBar = Runnable {
        hideStatusBar()
    }

    private val showActionBar = Runnable {
        a.actionBar?.show()
    }
    private var mVisible: Boolean = true
    private val mHideRunnable = Runnable { hide() }

    fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
            delayedHide()
        }
    }

    fun hide() {
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(showActionBar)
        a.actionBar?.hide()
        mVisible = false
        hideElements()

        mHideHandler.postDelayed(hideStatusBar, UI_ANIMATION_DELAY.toLong())
    }

    fun show() {
        // Show the system bar
        showStatusBar()
        mVisible = true
        showElements()

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(hideStatusBar)
        mHideHandler.postDelayed(showActionBar, UI_ANIMATION_DELAY.toLong())
    }

    fun delayedHide(delayMillis: Int = AUTO_HIDE_DELAY_MILLIS) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    private fun showStatusBar() {
        a.window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    private fun hideStatusBar() {
        a.window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    companion object {
        private val AUTO_HIDE = true
        private val AUTO_HIDE_DELAY_MILLIS = 1000
        private val UI_ANIMATION_DELAY = 300
    }
}