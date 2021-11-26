package uk.co.sentinelweb.cuer.core.mappers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

enum class Format { SECS, MILLIS }

expect class TimeFormatter() {
    fun formatTime(time: LocalDateTime, format: Format = Format.SECS): String
    fun formatTime(timeSecs: Float, format: Format = Format.SECS): String
    fun formatNow(format: Format = Format.SECS): String
    fun formatMillis(l: Long, format: Format = Format.MILLIS): String
    fun formatFrom(time: LocalDateTime, format: Format = Format.SECS): String
}

expect class DateTimeFormatter() {
    fun formatDateTime(d: LocalDateTime): String
    fun formatDate(d: LocalDate): String
    fun formatDateTimeNullable(d: LocalDateTime?): String
}
