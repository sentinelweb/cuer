package uk.co.sentinelweb.cuer.app.util.chromecast

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.*
import com.google.android.gms.common.images.WebImage
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.utils.PlayServicesUtils
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromeCastContract
import uk.co.sentinelweb.cuer.domain.MediaDomain


class ChromeCastWrapper(private val application: Application) : ChromeCastContract.Wrapper {

    fun initMediaRouteButton(view: View) {
        val mediaRouteButton = view as androidx.mediarouter.app.MediaRouteButton
        CastButtonFactory.setUpMediaRouteButton(view.context, mediaRouteButton)
    }

    fun initMenuMediaRouteButton(menu: Menu, @IdRes menuId: Int): MenuItem {
        return CastButtonFactory.setUpMediaRouteButton(application, menu, menuId)
    }

    fun checkPlayServices(activity: Activity, requestCode: Int, okFunc: () -> Unit) {
        PlayServicesUtils.checkGooglePlayServicesAvailability(
            activity, requestCode, { okFunc() })
    }

    fun getCastContext(): CastContext = CastContext.getSharedInstance(application)

    override fun killCurrentSession() = getCastContext().sessionManager.endCurrentSession(true)
    override fun getCastDeviceName() = getCastSession()?.castDevice?.friendlyName

    fun addSessionListener(listener: SessionManagerListener<CastSession>) =
        getCastContext().sessionManager.addSessionManagerListener(listener, CastSession::class.java)

    fun removeSessionListener(listener: SessionManagerListener<CastSession>) =
        getCastContext().sessionManager.addSessionManagerListener(listener, CastSession::class.java)

    fun buildCastMediaInfo(media: MediaDomain): MediaInfo {
        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)

        metadata.putString(MediaMetadata.KEY_TITLE, media.title ?: "No title")
        metadata.putString(
            MediaMetadata.KEY_SUBTITLE,
            media.description ?: "No description"
        )
        metadata.addImage(WebImage(Uri.parse((media.thumbNail ?: media.image)?.url)))

        return MediaInfo.Builder(media.url)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("videos/mp4")
            .setMetadata(metadata)
            .build()
    }

    fun isCastConnected() = getCastContext().castState == CastState.CONNECTED

    fun getCastSession() = getCastContext().sessionManager.currentCastSession

}