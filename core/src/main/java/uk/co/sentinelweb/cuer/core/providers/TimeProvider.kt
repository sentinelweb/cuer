package uk.co.sentinelweb.cuer.core.providers

import java.time.Instant
import java.time.LocalDateTime

class TimeProvider {
    fun instant() = Instant.now()

    fun localDateTime() = LocalDateTime.now()
}