package uk.co.sentinelweb.cuer.app.util.chromecast

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import androidx.mediarouter.media.MediaRouter
import com.google.android.gms.cast.CastDevice
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.common.images.WebImage
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.utils.PlayServicesUtils
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromecastContract
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromecastContract.Route
import uk.co.sentinelweb.cuer.domain.MediaDomain


class ChromeCastWrapper(private val application: Application) : ChromecastContract.Wrapper {

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

    override fun isCastConnected(): Boolean = getCastSession()?.isConnected ?: false

    fun getCastSession() = getCastContext().sessionManager.currentCastSession
    override fun getVolume(): Double = getCastSession()
        ?.castDevice
        ?.let { getRoute(it)?.volume?.toDouble() }
        ?: 100.0

    override fun getMaxVolume(): Double = getCastSession()
        ?.castDevice
        ?.let { getRoute(it)?.volumeMax?.toDouble() }
        ?: 100.0

    override fun setVolume(volume: Float) {
        getCastSession()?.volume = volume.toDouble()
    }

    override fun getMediaRouteIdForCurrentSession(): String? = getCastSession()
        ?.castDevice
        ?.let { getRoute(it) }
        ?.id

    override fun getMediaRouteForCurrentSession(): Route? = getCastSession()
        ?.castDevice
        ?.let { getRoute(it) }
        ?.run {
            Route(
                id = id,
                description = description,
                deviceName = name,
                connectionState = connectionState,
                volumeMax = volumeMax,
                volume = volumeMax
            )
        }


    /*
        description:Cuer
        name:Living Room TV
        connectionState:2
        presentationDisplay.name:null
        volume:20
        volumeMax:20
         */
    override fun logRoutes() {
        MediaRouter.getInstance(application).routes.forEach {
            Log.d(
                "MediaRoutes",
                """MediaRoutes:
                description:${it.description}
                id:${it.id}
                name:${it.name}
                connectionState:${it.connectionState}
                presentationDisplay.name:${it.presentationDisplay?.name}
                volume:${it.volume}
                volumeMax:${it.volumeMax}
            """.trimIndent()
            )
        }
    }

    fun getRoute(castDevice: CastDevice): MediaRouter.RouteInfo? =
        MediaRouter.getInstance(application)
            .routes
            .find { it.id.endsWith(castDevice.deviceId) }

    override fun logCastDevice() {
        getCastSession()?.castDevice?.also { castDevice ->
            Log.d(
                "CastDevice",
                """CastDevice:
                   deviceId: ${castDevice.deviceId}
                   friendlyName: ${castDevice.friendlyName}
                   deviceVersion: ${castDevice.deviceVersion}
                   inetAddress: ${castDevice.inetAddress}
                   isOnLocalNetwork: ${castDevice.isOnLocalNetwork}
                   modelName: ${castDevice.modelName}
                   servicePort: ${castDevice.servicePort}
                """.trimIndent()
            )
        }
    }
}
