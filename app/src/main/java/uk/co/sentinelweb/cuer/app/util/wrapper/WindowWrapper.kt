package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import android.view.View
import android.view.WindowInsets

class WindowWrapper {
    fun setDecorFitsSystemWindows(a: Activity, showNav: Boolean) {
        var flags = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE/*_STICKY*/)
        flags = if (!showNav) {
            (flags
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    //or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
        } else flags
        a.window.decorView.systemUiVisibility = flags
    }

    fun doOnApplyWindowInsets(v: View, f: (View, WindowInsets, InitialPadding) -> Unit) {
        // Create a snapshot of the view's padding state
        val initialPadding = recordInitialPaddingForView(v)
        // Set an actual OnApplyWindowInsetsListener which proxies to the given
        // lambda, also passing in the original padding state
        v.setOnApplyWindowInsetsListener { v, insets ->
            f(v, insets, initialPadding)
            // Always return the insets, so that children can also use them
            insets
        }
        // request some insets
        requestApplyInsetsWhenAttached(v)
    }

    data class InitialPadding(
        val left: Int, val top: Int,
        val right: Int, val bottom: Int
    )

    private fun recordInitialPaddingForView(view: View) = InitialPadding(
        view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
    )

    fun requestApplyInsetsWhenAttached(v: View) {
        if (v.isAttachedToWindow) {
            // We're already attached, just request as normal
            v.requestApplyInsets()
        } else {
            // We're not attached to the hierarchy, add a listener to
            // request when we are
            v.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    v.removeOnAttachStateChangeListener(this)
                    v.requestApplyInsets()
                }

                override fun onViewDetachedFromWindow(v: View) = Unit
            })
        }
    }
}