package uk.co.sentinelweb.cuer.core.providers

import kotlinx.datetime.*

interface TimeProvider {
    fun instant(): Instant

    fun localDateTime(): LocalDateTime
    fun currentTimeMillis(): Long
    fun timeZoneOffsetSecs(): UtcOffset
    fun getOffsetTime(millis: Long): LocalDateTime

    companion object {
        fun LocalDateTime.toInstant() = this.toInstant(TimeZone.currentSystemDefault())
        fun Instant.toLocalDateTime() = this.toLocalDateTime(TimeZone.currentSystemDefault())
    }
}