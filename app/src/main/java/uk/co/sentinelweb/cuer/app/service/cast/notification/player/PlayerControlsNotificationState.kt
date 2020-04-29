package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.PlayerStateUi

data class PlayerControlsNotificationState(
    var playState: PlayerStateUi = PlayerStateUi.UNKNOWN,
    var positionMs: Long = 0,
    var seekPositionMs: Long = 0,
    var durationMs: Long = 0,
    var title: String = ""
)