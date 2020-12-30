package uk.co.sentinelweb.cuer.app.util.wrapper

import android.content.Context
import android.content.res.Resources
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

class ResourceWrapper constructor(
    private val context: Context
) {

    val resources: Resources = context.resources

    fun getString(@StringRes id: Int) = context.getString(id)

    fun getString(@StringRes id: Int, vararg params: Any) = context.resources.getString(id, *params)

    fun getDimensionPixelSize(@DimenRes id: Int): Int = context.resources.getDimensionPixelSize(id)

    fun getDrawable(@DrawableRes id: Int, @ColorRes tint: Int?) =
        ContextCompat.getDrawable(context, id)?.let { d ->
            tint?.let { DrawableCompat.setTint(d, it) }
            d
        } ?: throw Exception("Drawable doesn't exist")
}
