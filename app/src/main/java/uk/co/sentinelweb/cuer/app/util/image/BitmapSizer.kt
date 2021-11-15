package uk.co.sentinelweb.cuer.app.util.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

class BitmapSizer() {
    fun checkSize(bytes: ByteArray, maxDim: Int): ByteArray {
        val b = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        return if (b.width > maxDim || b.height > maxDim) {
            val portrait = b.width < b.height
            val aspect = b.width.toFloat() / b.height
            val otherDim = (maxDim * (if (portrait) aspect else 1f / aspect)).toInt()
            val resized = Bitmap.createScaledBitmap(
                b,
                if (portrait) otherDim else maxDim,
                if (portrait) maxDim else otherDim,
                false
            )
            val baos = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.PNG, 100, baos) //bm is the bitmap object
            baos.toByteArray()
        } else bytes

    }
}