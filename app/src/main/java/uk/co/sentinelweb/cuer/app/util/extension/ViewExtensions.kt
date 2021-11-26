package uk.co.sentinelweb.cuer.app.util.extension.view

import android.animation.ObjectAnimator
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible

fun View.fade(visible: Boolean) {
    val targetAlpha = if (visible) 1f else 0f
    val startAlpha = 1 - targetAlpha
    if (this.alpha == startAlpha && this.tag == null) {
        val alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", targetAlpha)
        alphaAnimator.duration = 300
        alphaAnimator.doOnEnd { this.tag = null }
        this.tag = alphaAnimator
        alphaAnimator.start()
    }
}

fun View.fadeIn() {
    val alphaAnimation = ObjectAnimator.ofFloat<View>(this, View.ALPHA, this.alpha, 1f)
    alphaAnimation.duration = 300
    alphaAnimation.doOnStart { this.isVisible = true }
    alphaAnimation.start()
}


fun View.fadeOut() {
    val alphaAnimation = ObjectAnimator.ofFloat<View>(this, View.ALPHA, this.alpha, 0f)
    alphaAnimation.duration = 300
    alphaAnimation.doOnEnd { this.isVisible = false }
    alphaAnimation.start()
}