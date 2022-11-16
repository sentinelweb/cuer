package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable

@Serializable
enum class PlayerStateDomain {
    UNKNOWN,
    UNSTARTED,
    ENDED,
    PLAYING,
    PAUSED,
    BUFFERING,
    VIDEO_CUED,
    ERROR
}