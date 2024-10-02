package uk.co.sentinelweb.cuer.app.util.glide

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import org.koin.core.component.KoinComponent
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.StatusBarColorWrapper

class GlideStatusColorLoadListener(
    private val statusBarColorWrapper: StatusBarColorWrapper,
) : RequestListener<Drawable?>, KoinComponent {

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable?>?,
        isFirstResource: Boolean
    ): Boolean {
        statusBarColorWrapper.setStatusBarColorResource(R.color.primary_variant)
        return true
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable?>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        if (resource is BitmapDrawable) {
            statusBarColorWrapper.changeStatusBarColor(resource.bitmap)
        }
        return false
    }
}
