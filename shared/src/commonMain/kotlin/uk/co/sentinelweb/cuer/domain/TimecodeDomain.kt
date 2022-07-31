package uk.co.sentinelweb.cuer.domain

import kotlin.time.Duration

data class TimecodeDomain(
    val position: Long,
    val title: String,
    val extractRegion: Pair<Int, Int>? = null
)