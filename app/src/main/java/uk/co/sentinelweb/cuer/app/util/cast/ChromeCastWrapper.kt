package uk.co.sentinelweb.cuer.app.util.cast

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.utils.PlayServicesUtils

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
}