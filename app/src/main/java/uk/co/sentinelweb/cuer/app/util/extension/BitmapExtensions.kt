package uk.co.sentinelweb.cuer.app.util.extension

import android.graphics.*

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