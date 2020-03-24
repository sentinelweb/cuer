package uk.co.sentinelweb.cuer.app.util.cast

import android.app.Activity
import android.app.Application
import android.view.View
import com.google.android.gms.cast.framework.CastButtonFactory
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.utils.PlayServicesUtils

class ChromeCastWrapper(androidApplication: Application) {// TODO check change to context

    fun initMediaRouteButton(view: View) {
        val mediaRouteButton = view as androidx.mediarouter.app.MediaRouteButton
        CastButtonFactory.setUpMediaRouteButton(view.context, mediaRouteButton)
    }

    fun checkPlayServices(activity: Activity, requestCode: Int, okFunc: () -> Unit) {
        PlayServicesUtils.checkGooglePlayServicesAvailability(
            activity, requestCode, Runnable { okFunc() })
    }
}