package uk.co.sentinelweb.cuer.core.mappers

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

// todo remove and make ktor converters
expect class TimeStampMapper(log: LogWrapper) {
    val log: LogWrapper

    fun parseTimestamp(dateString: String): LocalDateTime?

    fun parseTimestampInstant(dateString: String): Instant?

    fun toTimestamp(date: LocalDateTime): String

    fun toTimestamp(date: Instant): String

    fun parseDuration(duration: String): Long?

    fun toTimestampSimple(date: LocalDateTime): String

    fun toTimeStampSimple(ms: Long): String

    fun nanosToLocalDateTime(ns: Long): LocalDateTime?
}