package uk.co.sentinelweb.cuer.app.ui.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import com.google.android.material.R.style.Widget_MaterialComponents_CardView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapeAppearancePathProvider

/**
 * A text view that clips the content of any shape
 */
class MaskedCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : MaterialCardView(context, attrs, defStyle) {
    @SuppressLint("RestrictedApi")
    private val pathProvider = ShapeAppearancePathProvider()
    private val path: Path = Path()
    private val shapeAppearance: ShapeAppearanceModel =
        ShapeAppearanceModel.builder(
            context, attrs, defStyle, Widget_MaterialComponents_CardView
        ).build()

    private val rectF = RectF(0f, 0f, 0f, 0f)

    override fun onDraw(canvas: Canvas) {
        canvas.clipPath(path)
        super.onDraw(canvas)
    }

    @SuppressLint("RestrictedApi")
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        rectF.right = w.toFloat()
        rectF.bottom = h.toFloat()
        pathProvider.calculatePath(shapeAppearance, 1f, rectF, path)
        super.onSizeChanged(w, h, oldw, oldh)
    }
}