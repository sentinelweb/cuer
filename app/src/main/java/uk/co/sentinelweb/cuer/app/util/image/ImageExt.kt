package uk.co.sentinelweb.cuer.app.util.image

import android.graphics.Bitmap
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager


fun RequestManager.loadFirebaseOrOtherUrl(url: String, imageProvider: ImageProvider) = this.run {
    imageProvider.doLoad(this, url)
}

fun RequestBuilder<Bitmap>.loadFirebaseOrOtherUrl(url: String, imageProvider: ImageProvider) = this.run {
    imageProvider.doLoad(this, url)
}