package uk.co.sentinelweb.cuer.core.mappers

import kotlinx.datetime.*
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

// todo test this on ios platform
actual class TimeStampMapper constructor(
    private val log: LogWrapper
) {

    private val timestampDateFormatter: NSDateFormatter
        get() = NSDateFormatter().apply {
            locale = NSLocale("GMT")
            dateFormat = TIMESTAMP_PATTERN
        }

    private val simpleDateFormatter: NSDateFormatter
        get() = NSDateFormatter().apply {
            locale = NSLocale("GMT")
            dateFormat = SIMPLE_DATETIME_PATTERN
        }

    actual fun parseTimestamp(dateString: String): LocalDateTime? {
        return timestampDateFormatter.dateFromString(dateString)?.toKotlinInstant()
            ?.toLocalDateTime(TimeZone.currentSystemDefault())
    }

    actual fun parseTimestampInstant(dateString: String): Instant? {
        return timestampDateFormatter.dateFromString(dateString)?.toKotlinInstant()
    }

    actual fun toTimestamp(date: LocalDateTime): String {// todo check time included?
        val dateComponents = date.toNSDateComponents()
        return dateComponents.date
            ?.let { timestampDateFormatter.stringFromDate(date = it) }
            ?: "BAD DATE: $date"
    }

    actual fun toTimestamp(date: Instant): String =
        timestampDateFormatter.stringFromDate(date = date.toNSDate())

    actual fun parseDuration(duration: String): Long? = parseDurationPrivate(duration)

    actual fun toTimestampSimple(date: LocalDateTime): String {
        val dateComponents = date.toNSDateComponents()
        return dateComponents.date
            ?.let { simpleDateFormatter.stringFromDate(date = it) }
            ?: "BAD DATE: $date"
    }

    actual fun toTimeStampSimple(ms: Long): String = TODO()

    actual fun nanosToLocalDateTime(ns: Long): LocalDateTime? = TODO()

    // from: https://stackoverflow.com/questions/48030681/how-to-parse-a-iso-8601-duration-format-in-swift
    private fun parseDurationPrivate(duration: String): Long {
        val durationMod = duration.substring(1)//"P"
        var (days, remainderDays) = chompNumber(durationMod, "D")
        return if (remainderDays.startsWith("T")) {
            remainderDays = remainderDays.substring(1)
            val (hours, remainderHours) = chompNumber(remainderDays, "H")
            val (minutes, remainderMins) = chompNumber(remainderHours, "M")
            val (seconds, _) = chompNumber(remainderMins, "S")
            (days * DAYS + hours * HOURS + minutes * MINS + seconds) * MS
        } else {
            days * DAYS * MS
        }
    }

    private fun chompNumber(buffer: String, token: String): Pair<Long, String> {
        var bufferMod = buffer
        val tokenIndex = bufferMod.indexOf(token)
        val parsedNumber = if (tokenIndex > -1) {
            val number = bufferMod.substring(0, tokenIndex)
                .let {
                    val dotIndex = it.indexOf(".")
                    if (dotIndex > -1) it.substring(0, dotIndex) else it
                }
            val digit = number.toLong()
            bufferMod = bufferMod.substring(tokenIndex + 1)
            digit
        } else {
            0
        }
        return Pair(parsedNumber, bufferMod)
    }

    companion object {
        private const val TIMESTAMP_PATTERN = "uuuu-MM-dd'T'HH:mm:ss[.n]'Z'"
        private const val SIMPLE_DATETIME_PATTERN = "uuuu-MM-dd_HH:mm:ss"
        private const val DAYS = 24 * 60 * 60
        private const val HOURS = 60 * 60
        private const val MINS = 60
        private const val MS = 1000
    }

}