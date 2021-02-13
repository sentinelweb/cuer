package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import android.view.View

class WindowWrapper {
    fun setDecorFitsSystemWindows(a: Activity, showNav: Boolean) {
        var flags = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        flags = if (showNav) {
            (flags
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        } else flags
        a.window.decorView.systemUiVisibility = flags
    }
}