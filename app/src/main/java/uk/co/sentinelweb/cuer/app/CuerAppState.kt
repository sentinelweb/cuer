package uk.co.sentinelweb.cuer.app

import android.support.v4.media.session.MediaSessionCompat
import uk.co.sentinelweb.cuer.domain.MediaDomain

// todo think about this nice to have a global state injectable as needed
data class CuerAppState constructor(
    var connected: Boolean = false,
    var castNotificationChannelId: String? = null,
    var mediaSession: MediaSessionCompat? = null,
    var currentMedia: MediaDomain? = null
)