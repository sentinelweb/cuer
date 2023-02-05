package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import uk.co.sentinelweb.cuer.app.R

interface SnackbarWrapper {
    fun make(
        msg: String,
        length: Int = LENGTH_SHORT,
        actionText: String? = null,
        action: ((View) -> Unit)? = null
    ): Snackbar

    fun makeError(
        msg: String,
        length: Int = LENGTH_LONG,
        actionText: String? = null,
        action: ((View) -> Unit)? = null
    ): Snackbar
}

class AndroidSnackbarWrapper constructor(
    private val a: Activity,
    private val res: ResourceWrapper
) : SnackbarWrapper {

    private val errorDrawable: Drawable by lazy {
        res.getDrawable(
            R.drawable.ic_error,
            R.color.white,
            R.dimen.snackbar_icon,
            1.4f
        )
    }

    override fun make(
        msg: String,
        length: Int,
        actionText: String?,
        action: ((View) -> Unit)?
    ) = Snackbar.make(a.findViewById(android.R.id.content), msg, length)
        .setBackgroundTint(res.getColor(R.color.snackbar_info_background))
        .setTextColor(res.getColor(R.color.text_primary))
        .setActionTextColor(res.getColor(R.color.white))
        .apply { if (actionText != null) setAction(actionText, action) }

    override fun makeError(
        msg: String,
        length: Int,
        actionText: String?,
        action: ((View) -> Unit)?
    ): Snackbar {
        val text = SpannableString("  $msg")
            .apply { res.replaceSpannableIcon(this, errorDrawable, 0, 1, ImageSpan.ALIGN_BOTTOM) }
        return Snackbar.make(a.findViewById(android.R.id.content), text, LENGTH_SHORT)
            .setBackgroundTint(res.getColor(R.color.error_snackbar))
            .setTextColor(res.getColor(R.color.white))
            .setActionTextColor(res.getColor(R.color.white))
            .apply { if (actionText != null) setAction(actionText, action) }
    }
}

fun Snackbar.positionAbovePlayer(): Snackbar {
    val params = getView().getLayoutParams() as FrameLayout.LayoutParams
    //params.gravity = Gravity.TOP
    params.bottomMargin = getView().resources.getDimensionPixelSize(R.dimen.snackbar_main_bottom_padding)
    view.setLayoutParams(params)
    return this
}
