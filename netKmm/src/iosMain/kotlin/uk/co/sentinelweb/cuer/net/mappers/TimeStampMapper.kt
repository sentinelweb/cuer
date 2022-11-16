package uk.co.sentinelweb.cuer.net.mappers

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

    actual fun toTimestamp(date: Instant): String {
        val date = date.toNSDate()
        return date
            .let { timestampDateFormatter.stringFromDate(date = it) }
    }

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
        var durationMod = duration
        if (durationMod.startsWith("PT")) {
            durationMod = durationMod.substring(2)
        } else throw IllegalArgumentException("duration doesn't start with PT: $duration")
        val (hour, remainder) = chompNumber(durationMod, "H")
        val (minute, remainder1) = chompNumber(remainder, "M")
        val (second, remainder2) = chompNumber(remainder1, "S")
        return hour * 3600 + minute * 60 + second
    }

    private fun chompNumber(durationMod: String, unit: String): Pair<Long, String> {
        var durationMod1 = durationMod
        val hourIndex = durationMod1.indexOf(unit)
        val hour = if (hourIndex > -1) {
            val digit = durationMod1.substring(0, hourIndex).toLong()
            durationMod1 = durationMod1.substring(hourIndex)
            digit
        } else {
            0
        }
        return Pair(hour, durationMod1)
    }


    companion object {
        private const val TIMESTAMP_PATTERN = "uuuu-MM-dd'T'HH:mm:ss[.n]'Z'"
        private const val SIMPLE_DATETIME_PATTERN = "uuuu-MM-dd_HH:mm:ss"
    }

}