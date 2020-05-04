package uk.co.sentinelweb.cuer.app.util.cast

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.common.images.WebImage
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.utils.PlayServicesUtils
import uk.co.sentinelweb.cuer.domain.MediaDomain
import kotlin.math.min


class ChromeCastWrapper(val application: Application) {// TODO check change to context

    fun initMediaRouteButton(view: View) {
        val mediaRouteButton = view as androidx.mediarouter.app.MediaRouteButton
        CastButtonFactory.setUpMediaRouteButton(view.context, mediaRouteButton)
    }

    fun initMenuMediaRouteButton(menu: Menu, @IdRes menuId: Int): MenuItem {
        return CastButtonFactory.setUpMediaRouteButton(application, menu, menuId)
    }

    fun checkPlayServices(activity: Activity, requestCode: Int, okFunc: () -> Unit) {
        PlayServicesUtils.checkGooglePlayServicesAvailability(
            activity, requestCode, Runnable { okFunc() })
    }

    fun getCastContext() : CastContext = CastContext.getSharedInstance(application)

    fun killCurrentSession() = getCastContext().sessionManager.endCurrentSession(true)

    // from: https://code.tutsplus.com/tutorials/google-play-services-google-cast-v3-and-media--cms-26893
    fun getRemoteClient() =
        CastContext.getSharedInstance(application).sessionManager.currentCastSession?.remoteMediaClient

    fun buildCastMediaInfo(media: MediaDomain): MediaInfo {
        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)

        metadata.putString(MediaMetadata.KEY_TITLE, media.title)
        metadata.putString(
            MediaMetadata.KEY_SUBTITLE,
            media.description?.substring(0, min(media.description?.length ?: 0, 50))
        )
        metadata.addImage(WebImage(Uri.parse("https://www.stateofdigital.com/wp-content/uploads/2012/01/slap-on-wrist.jpg")))

        return MediaInfo.Builder(media.url)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("videos/mp4")
            .setMetadata(metadata)
            .build()
    }
}