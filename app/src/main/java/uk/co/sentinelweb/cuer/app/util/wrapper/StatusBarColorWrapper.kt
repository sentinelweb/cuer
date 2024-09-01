package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import android.graphics.Bitmap
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.palette.graphics.Palette

class StatusBarColorWrapper(private val activity: Activity) {

    fun changeStatusBarColor(imageBitmap: Bitmap) {
        val croppedBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.width, 20)

        Palette.from(croppedBitmap).generate { palette ->
            palette?.dominantSwatch?.let {
                setStatusBarColor(it.rgb)
            }
        }
    }

    private fun setStatusBarColor(color: Int) {
        val window: Window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
    }

    fun setStatusBarColorResource(@ColorRes colorRes: Int) {
        val window: Window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = activity.getColor(colorRes)
    }
}
