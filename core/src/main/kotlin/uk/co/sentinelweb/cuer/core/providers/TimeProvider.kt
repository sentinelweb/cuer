package uk.co.sentinelweb.cuer.core.providers

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime

class TimeProvider {
    fun instant() = Instant.now()

    fun localDateTime() = LocalDateTime.now()

    fun currentTimeMillis() = System.currentTimeMillis()

    fun timeZomeOffsetSecs() = OffsetDateTime.now().getOffset().totalSeconds
}