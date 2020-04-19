package uk.co.sentinelweb.klink.util.extension

import android.animation.ObjectAnimator
import android.view.View
import androidx.core.animation.doOnEnd

fun View.fade(i: Boolean) {
    val targetAlpha = if (i) 1f else 0f
    val startAlpha = 1 - targetAlpha
    if (this.alpha == startAlpha && this.tag == null) {
        val alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", targetAlpha)
        alphaAnimator.duration = 100
        alphaAnimator.doOnEnd { this.tag = null }
        this.tag = alphaAnimator
        alphaAnimator.start()

    }
}
