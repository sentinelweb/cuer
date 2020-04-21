package uk.co.sentinelweb.cuer.app.util.cast.listener

import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract

data class YouTubePlayerListenerState constructor(
    var playState: CastPlayerContract.PlayerStateUi = CastPlayerContract.PlayerStateUi.UNKNOWN,
    var positionSec: Float = 0f,
    var durationSec: Float = 0f,
    var title: String = ""
)