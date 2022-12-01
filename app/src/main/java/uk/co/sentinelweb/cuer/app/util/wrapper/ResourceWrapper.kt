package uk.co.sentinelweb.cuer.app.util.wrapper

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.TypedValue
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.shape.ShapeAppearanceModel
import uk.co.sentinelweb.cuer.app.ui.common.resources.Color
import uk.co.sentinelweb.cuer.app.ui.common.resources.Icon
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringResource
import java.io.InputStream


class ResourceWrapper constructor(
    private val context: Context,
) : StringDecoder {

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

    @ColorInt
    fun getColor(color: Color): Int {
        val id = getColorResourceId(color)
        return ContextCompat.getColor(context, id)
    }

    // todo check what happen when id isnt found
    @ColorRes
    fun getColorResourceId(color: Color) =
        resources.getIdentifier(color.name, "color", context.getPackageName())

    // todo check what happen when id isnt found
    @DrawableRes
    fun getDrawableResourceId(icon: Icon) =
        resources.getIdentifier(icon.name, "drawable", context.getPackageName())

    // todo check what happen when id isnt found
    @StringRes
    fun getStringResourceId(stringRes: StringResource) =
        resources.getIdentifier(stringRes.name, "string", context.getPackageName())

    override fun getString(res: StringResource): String =
        getStringResourceId(res)
            .let { getString(it) }

    override fun getString(res: StringResource, params: List<String>): String =
        getStringResourceId(res)
            .let { getString(it, *params.toTypedArray()) }


    @DrawableRes
    fun getStringResourceId(icon: Icon) =
        resources.getIdentifier(icon.name, "drawable", context.getPackageName())

    @ColorRes
    fun getColorAttr(@AttrRes id: Int): Int {
        val typedValue = TypedValue()
        context.getTheme().resolveAttribute(id, typedValue, true)
        return typedValue.resourceId
    }

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
        scale: Float = 1f,
    ): Drawable {
        val textPixelSize = (getDimensionPixelSize(textSize) * scale).toInt()
        return getDrawable(icon, tint).apply { setBounds(0, 0, textPixelSize, textPixelSize); }
    }

    fun replaceSpannableIcon(
        string: SpannableString,
        d: Drawable,
        start: Int,
        end: Int,
        align: Int = ImageSpan.ALIGN_BOTTOM,
    ) {
        string.apply {
            setSpan(
                ImageSpan(d, align),
                start, end,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
    }

    fun getIntArray(@ArrayRes id: Int): List<Int> = resources.getIntArray(id).toList()

    fun getAssetString(path: String): String? =
        try {
            val inputStream: InputStream = context.assets.open(path)
            inputStream.bufferedReader().use { it.readText() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }

    fun getShapeModel(id: Int) = ShapeAppearanceModel.builder(context, 0, id).build()
    fun getColorStateList(id: Int): ColorStateList? = ContextCompat.getColorStateList(context, id)

}
