package uk.co.sentinelweb.cuer.app.util.extension

import android.content.Context
import android.graphics.*
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapeAppearancePathProvider
import uk.co.sentinelweb.cuer.app.R

// https://www.tutorialspoint.com/android-how-to-crop-circular-area-from-bitmap
fun Bitmap.getCircularBitmap(): Bitmap {
    // Calculate the circular bitmap width with border
    val squareBitmapWidth = Math.min(this.width, this.height)
    // Initialize a new instance of Bitmap
    val dstBitmap = Bitmap.createBitmap(
        squareBitmapWidth,  // Width
        squareBitmapWidth,  // Height
        Bitmap.Config.ARGB_8888 // Config
    )
    val canvas = Canvas(dstBitmap)
    // Initialize a new Paint instance
    val paint = Paint()
    paint.setAntiAlias(true)
    val rect = Rect(0, 0, squareBitmapWidth, squareBitmapWidth)
    val rectF = RectF(rect)
    canvas.drawOval(rectF, paint)
    paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
    // Calculate the left and top of copied bitmap
    val left = (squareBitmapWidth - this.width) / 2.toFloat()
    val top = (squareBitmapWidth - this.height) / 2.toFloat()
    canvas.drawBitmap(this, left, top, paint)
    // Free the native object associated with this bitmap.
    this.recycle()
    // Return the circular bitmap
    return dstBitmap
}

fun Bitmap.cropShapedBitmap(c: Context): Bitmap {
    // Calculate the circular bitmap width with border
    val squareBitmapWidth = Math.min(this.width, this.height)
    // Initialize a new instance of Bitmap
    val dstBitmap = Bitmap.createBitmap(
        squareBitmapWidth,  // Width
        squareBitmapWidth,  // Height
        Bitmap.Config.ARGB_8888 // Config
    )
    val canvas = Canvas(dstBitmap)
    // Initialize a new Paint instance
    val paint = Paint()
    paint.setAntiAlias(true)
    val pathProvider = ShapeAppearancePathProvider()
    val path = Path()
    val shapeModel = ShapeAppearanceModel.builder(c, R.style.ShapeAppearance_Item_Icon, 0).build()
    val rect = Rect(0, 0, squareBitmapWidth, squareBitmapWidth)
    val rectF = RectF(rect)
    pathProvider.calculatePath(shapeModel, 1f, rectF, path)
    canvas.drawPath(path, paint)
//    canvas.drawOval(rectF, paint)
    paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
    // Calculate the left and top of copied bitmap
    val left = (squareBitmapWidth - this.width) / 2.toFloat()
    val top = (squareBitmapWidth - this.height) / 2.toFloat()
    canvas.drawBitmap(this, left, top, paint)
    // Free the native object associated with this bitmap.
    this.recycle()
    // Return the circular bitmap
    return dstBitmap
}