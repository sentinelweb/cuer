package uk.co.sentinelweb.cuer.domain

data class TimecodeDomain(
    val position: Long,
    val title: String,
    val extractRegion: Pair<Int, Int>? = null
)
