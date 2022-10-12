package uk.co.sentinelweb.cuer.app.ui.common.views

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import androidx.core.view.ViewCompat

class SeekBarGestureExclude @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.seekBarStyle
) : androidx.appcompat.widget.AppCompatSeekBar(context, attrs, defStyle) {

    private fun updateGestureExclusion() {
        // Skip this call if we're not running on Android 10+
        if (Build.VERSION.SDK_INT < 29) return
        val rect = Rect()
        this.getGlobalVisibleRect(rect)
        // Finally pass our updated list of rectangles to the system
        ViewCompat.setSystemGestureExclusionRects(this, listOf(rect))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        updateGestureExclusion()
    }
}
