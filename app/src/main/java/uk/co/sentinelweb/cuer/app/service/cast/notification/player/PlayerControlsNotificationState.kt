package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import android.graphics.Bitmap
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

data class PlayerControlsNotificationState(
    var playState: PlayerStateDomain = PlayerStateDomain.UNKNOWN,
    var positionMs: Long = 0,
    var seekPositionMs: Long = 0,
    var durationMs: Long = 0,
    var title: String = "",
    var media: MediaDomain? = null,
    var bitmap: Bitmap? = null
)