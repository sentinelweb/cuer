package uk.co.sentinelweb.cuer.app.util.image

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import uk.co.sentinelweb.cuer.app.CuerApp
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import java.lang.ref.WeakReference

interface ArtLoaderReceiver {
    var thumb: WeakReference<Bitmap>?
    var image: WeakReference<Bitmap>?
    var thumbDomain: ImageDomain?
    var imageDomain: ImageDomain?
}

class ArtLoader constructor(
    private val app: CuerApp,
    private val state: ArtLoaderReceiver
) {
    fun loadMedia(media: MediaDomain, thumb: Boolean = false, onLoad: (bitmap: Bitmap) -> Unit) {
        media.thumbNail?.let {
            Picasso.get().load(it.url).into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    state.thumb = bitmap?.let { WeakReference(it) }
                    if (thumb) {
                        bitmap?.apply { onLoad(this) }
                    }
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
            })
        }
    }
}