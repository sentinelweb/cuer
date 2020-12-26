package uk.co.sentinelweb.cuer.app.ui.common.chip

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.scale
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.chip.Chip
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST_SELECT
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseDefaultImageProvider
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import java.security.MessageDigest


class ChipCreator(
    private val c: Context,
    private val imageProvider: FirebaseDefaultImageProvider,
    private val res: ResourceWrapper
) {

    fun create(model: ChipModel, parent: ViewGroup): Chip? = when (model.type) {
        PLAYLIST_SELECT -> {
            (LayoutInflater.from(c)
                .inflate(R.layout.playlist_chip_select, parent, false) as Chip).apply {
                tag = model
            }
        }
        PLAYLIST -> {
            (LayoutInflater.from(c)
                .inflate(R.layout.playlist_chip, parent, false) as Chip).apply {
                tag = model
                text = model.text
                model.thumb?.let {
                    Glide.with(this)
                        .asBitmap()
                        .load(imageProvider.makeRef(it))
                        .transform(CropTransformation(it.url))
                        .into(ChipLoadTarget(this))
                }
            }
        }
    }

    private inner class ChipLoadTarget(private val chip: Chip) : CustomTarget<Bitmap?>() {
        override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap?>?) {
            chip.chipIcon = BitmapDrawable(res.resources, bitmap)
        }

        override fun onLoadCleared(placeholder: Drawable?) {

        }
    }

    // from https://tech.okcupid.com/cropping-bitmaps-with-custom-glide-transformations/
    private inner class CropTransformation(
        private val url: String
    ) : BitmapTransformation() {

        private val targetSize = res.getDimensionPixelSize(R.dimen.chip_thumb_icon_size)

        fun getId(): String {

            return CropTransformation::class.java.name +
                    "url=$url" +
                    "dim=$targetSize" +
                    "-chip"
        }

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(getId().toByteArray())
        }

        override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
            // Get the width and height of the bitmap we'll be cropping from.
            val bitmapWidth = toTransform.width
            val bitmapHeight = toTransform.height

            val dimension = bitmapHeight / 2
            val left = (bitmapWidth - dimension) / 2
            val top = (bitmapHeight - dimension) / 2

            return Bitmap
                .createBitmap(toTransform, left, top, dimension, dimension)
                .getCircularBitmap()
                .scale(targetSize, targetSize)
        }
    }

    // https://www.tutorialspoint.com/android-how-to-crop-circular-area-from-bitmap
    protected fun Bitmap.getCircularBitmap(): Bitmap {
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
}