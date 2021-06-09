package uk.co.sentinelweb.cuer.app
import android.support.v4.media.session.MediaSessionCompat

data class CuerAppState constructor(
    var castNotificationChannelId: String? = null,
    var remoteNotificationChannelId: String? = null,
    var mediaSession: MediaSessionCompat? = null
)