package uk.co.sentinelweb.cuer.core.providers

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime

//import java.time.Instant
//import java.time.LocalDateTime
//import java.time.OffsetDateTime

class TimeProvider {
    fun instant() = Clock.System.now()

    fun localDateTime() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    fun currentTimeMillis() = Clock.System.now().toEpochMilliseconds()

    fun timeZomeOffsetSecs() = TimeZone.currentSystemDefault().offsetAt(instant())// todo might be broken
    //OffsetDateTime.now().getOffset().getTotalSeconds()
}