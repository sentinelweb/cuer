package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

// Hide the status bar
class HideStatusBarWrapper() {

    fun hide(a:Activity) {
        WindowCompat.setDecorFitsSystemWindows(a.window, false)
        val controller = WindowInsetsControllerCompat(a.window, a.window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
    }
}
