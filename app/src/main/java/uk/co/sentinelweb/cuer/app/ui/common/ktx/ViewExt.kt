package uk.co.sentinelweb.cuer.app.ui.common.ktx

import android.widget.TextView

fun TextView.scaleDrawableLeftSize(scale: Float) {
    val img = compoundDrawables.get(0) //left
    val bounds = img.bounds
    val left = (bounds.left * scale).toInt()
    val top = (bounds.top * scale).toInt()
    val right = (bounds.right * scale).toInt()
    val bottom = (bounds.bottom * scale).toInt()
    img.setBounds(left, top, right, bottom)
    setCompoundDrawables(img, null, null, null)
}
