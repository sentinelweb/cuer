package uk.co.sentinelweb.cuer.app.util.cast

import android.app.Application
import android.view.View

class ChromeCastWrapper(androidApplication: Application) {// TODO check change to context

    fun initMediaRouteButton(view:View) {
        // MediaRouteButtonUtils.initMediaRouteButton(media_route_button)// todo maybe have to go in player_controls_view
        MediaRouteButtonUtils.initMediaRouteButton(view as androidx.mediarouter.app.MediaRouteButton)
    }
}