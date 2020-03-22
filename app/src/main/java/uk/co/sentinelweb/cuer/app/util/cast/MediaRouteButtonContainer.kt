package uk.co.sentinelweb.cuer.app.util.cast

import androidx.mediarouter.app.MediaRouteButton

interface MediaRouteButtonContainer {
    fun addMediaRouteButton(mediaRouteButton: MediaRouteButton)
    fun removeMediaRouteButton(mediaRouteButton: MediaRouteButton)
}