package uk.co.sentinelweb.cuer.core.providers

import kotlinx.datetime.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime

class TimeProvider {
    fun instant() = Instant.now()

    fun localDateTime() = LocalDateTime.now()

    fun currentTimeMillis() = Clock.System.now().toEpochMilliseconds()

    fun timeZomeOffsetSecs() = OffsetDateTime.now().getOffset().getTotalSeconds()
}