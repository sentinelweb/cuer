package uk.co.sentinelweb.cuer.app.util.wrapper

import uk.co.sentinelweb.cuer.domain.MediaDomain

interface PlatformLaunchWrapper {
    fun canLaunchVideo(): Boolean
    fun canLaunchVideoWithOptions(): Boolean
    fun launchChannel(media: MediaDomain): Boolean
    fun launchChannel(id: String): Boolean
    fun launchPlaylist(id: String): Boolean
    fun launchVideo(media: MediaDomain): Boolean
    fun launchVideoSystem(platformId: String): Boolean
    fun launchVideoWithTimeSystem(media: MediaDomain): Boolean
    fun launch(address: String): Boolean
}