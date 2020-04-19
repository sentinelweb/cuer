package com.roche.mdas.util.wrapper

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import uk.co.sentinelweb.cuer.app.R

class ToolbarWrapper(
    private val activity: AppCompatActivity
) {
    private lateinit var toolbar: Toolbar

    fun setup(toolbar: Toolbar) {
        this.toolbar = toolbar
        activity.setSupportActionBar(toolbar)
    }

    fun hideToolbar() {
        toolbar.isVisible = false
    }

    fun showToolbar() {
        toolbar.isVisible = true
    }

    fun setStatusBarLight() {
        activity.window.statusBarColor = activity.getColor(R.color.white)
        activity.window.decorView.apply {
            systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    fun setStatusBarColorDefault() {
        activity.window.statusBarColor = activity.getColor(R.color.colorPrimaryDark)
        activity.window.decorView.apply {
            systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}
