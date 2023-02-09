package uk.co.sentinelweb.cuer.core.providers

import kotlinx.datetime.*
import kotlin.time.Duration.Companion.seconds

//import java.time.Instant
//import java.time.LocalDateTime
//import java.time.OffsetDateTime

class TimeProvider {
    fun instant() = Clock.System.now()

    fun localDateTime() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    fun currentTimeMillis() = Clock.System.now().toEpochMilliseconds()

    fun timeZomeOffsetSecs() =
        TimeZone.currentSystemDefault().offsetAt(instant()) // todo might be broken

    fun getOffsetTime(millis: Long): LocalDateTime {
        val timeZone = TimeZone.currentSystemDefault()
        val now = localDateTime().toInstant(timeZone)
        val duration = (millis / 1000L).seconds
        return now.minus(duration).toLocalDateTime(timeZone)
    }

}