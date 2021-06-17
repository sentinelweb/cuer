package uk.co.sentinelweb.cuer.app.util.wrapper

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

class ResourceWrapper constructor(
    private val context: Context
) {

    val resources: Resources = context.resources

    val pixelDensity: Float by lazy {
        resources.displayMetrics.density
    }

    val screenWidth: Int by lazy {
        resources.displayMetrics.widthPixels
    }
    val screenHeight: Int by lazy {
        resources.displayMetrics.heightPixels
    }

    fun getString(@StringRes id: Int) = context.getString(id)

    fun getString(@StringRes id: Int, vararg params: Any) = context.resources.getString(id, *params)

    @ColorInt
    fun getColor(@ColorRes id: Int) = ContextCompat.getColor(context, id)

    fun getDimensionPixelSize(@DimenRes id: Int): Int = context.resources.getDimensionPixelSize(id)

    fun getDimensionPixelSize(pixels: Float): Int = (pixelDensity * pixels).toInt()

    fun getDrawable(@DrawableRes id: Int, @ColorRes tint: Int?) =
        ContextCompat.getDrawable(context, id)?.let { d ->
            tint?.let { DrawableCompat.setTint(d, ContextCompat.getColor(context, it)) }
            d
        } ?: throw Exception("Drawable doesn't exist $id")

    fun getDrawable(@DrawableRes id: Int) =
        ContextCompat.getDrawable(context, id)
            ?: throw Exception("Drawable doesn't exist : $id")

    fun getDrawable(
        @DrawableRes icon: Int,
        @ColorRes tint: Int,
        @DimenRes textSize: Int,
        scale: Float = 1f
    ): Drawable {
        val textPixelSize = (getDimensionPixelSize(textSize) * scale).toInt()
        return getDrawable(icon, tint).apply { setBounds(0, 0, textPixelSize, textPixelSize); }
    }

    fun replaceSpannableIcon(
        string: SpannableString,
        d: Drawable,
        start: Int,
        end: Int
    ) {
        string.apply {
            setSpan(
                ImageSpan(d, ImageSpan.ALIGN_BOTTOM),
                start, end,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
    }

    fun getIntArray(@ArrayRes id: Int): List<Int> = resources.getIntArray(id).toList()
}
