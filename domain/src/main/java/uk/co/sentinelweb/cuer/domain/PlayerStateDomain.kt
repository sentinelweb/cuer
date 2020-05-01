package uk.co.sentinelweb.cuer.domain

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