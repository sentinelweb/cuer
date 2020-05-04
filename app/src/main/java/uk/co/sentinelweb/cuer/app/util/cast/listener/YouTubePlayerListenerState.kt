package uk.co.sentinelweb.cuer.app.util.cast.listener

import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

data class YouTubePlayerListenerState constructor(
    var playState: PlayerStateDomain = PlayerStateDomain.UNKNOWN,
    var positionSec: Float = 0f, // todo remove these mutate media
    var durationSec: Float = 0f, // todo remove these mutate media
    var currentMedia: MediaDomain? = null
)