package uk.co.sentinelweb.cuer.core.mappers

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

// todo remove and make ktor converters
expect class TimeStampMapper {

    fun parseTimestamp(dateString: String): LocalDateTime?

    fun parseTimestampInstant(dateString: String): Instant?

    fun toTimestamp(date: LocalDateTime): String

    fun toTimestamp(date: Instant): String

    fun parseDuration(duration: String): Long?

    fun toTimestampSimple(date: LocalDateTime): String

    fun toTimeStampSimple(ms: Long): String

    fun nanosToLocalDateTime(ns: Long): LocalDateTime?
}