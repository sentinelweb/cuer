package uk.co.sentinelweb.cuer.app.util.glide

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import org.koin.core.component.KoinComponent
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

/**
 * Dont know why this is needed but sometime images don't load properly this manual listner seem to work when just .into doesn't
 */
class GlideFallbackLoadListener constructor(
    private val imageView: ImageView,
    private val url: String,
    private val errDrawable: Drawable,
    log: LogWrapper? = null
) : RequestListener<Drawable?>, KoinComponent {

    private val _log = log ?: getKoin().get()
    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable?>?,
        isFirstResource: Boolean
    ): Boolean {
        imageView.setImageDrawable(errDrawable)
        _log.e("glide load failure url = $url", e)
        return true
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable?>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        //_log.d("glide loading suceess url = $url")
        imageView.setImageDrawable(resource)
        return true
    }
}