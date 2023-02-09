package uk.co.sentinelweb.cuer.core.mappers

import kotlinx.datetime.*
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale

actual class TimeFormatter {

    private val timestampWithMillisFormatter: NSDateFormatter
        get() = NSDateFormatter().apply {
            locale = NSLocale("GMT")
            dateFormat = TIME_PATTERN_MILLIS
        }

    private val timestampWithSecsFormatter: NSDateFormatter
        get() = NSDateFormatter().apply {
            locale = NSLocale("GMT")
            dateFormat = TIME_PATTERN_SECS
        }

    actual fun formatTime(time: LocalDateTime, format: Format): String =
        try {
            time.toNSDateComponents().date
                ?.let { formatter(format).stringFromDate(it).strip00() }
                ?: "-"
        } catch (e: Exception) {
            "-"
        }

    actual fun formatTime(timeSecs: Float, format: Format): String =
        try {
            formatter(format).stringFromDate(NSDate(timeSecs.toDouble())).strip00()
        } catch (e: Exception) {
            "-"
        }

    actual fun formatNow(format: Format): String =
        try {
            Clock.System.now().toLocalDateTime(TimeZone.UTC).toNSDateComponents().date
                ?.let { formatter(format).stringFromDate(it).strip00() }
                ?: "-"
        } catch (e: Exception) {
            "-"
        }

    actual fun formatMillis(l: Long, format: Format): String =
        try {
            formatter(format).stringFromDate(NSDate(l / 1000.0)).strip00()
        } catch (e: Exception) {
            "-"
        }

    actual fun formatFrom(time: LocalDateTime, format: Format): String = "N/A"
//        try {
//            Clock.System.now().minus(time.toInstant(TimeZone.UTC)).toLocalDateTime(TimeZone.UTC).toNSDateComponents().date
//                ?.let { formatter(format).stringFromDate(it).strip00() }
//                ?: "-"
//        } catch (e: Exception) {
//            "-"
//        }

    private fun formatter(format: Format): NSDateFormatter = when (format) {
        Format.MILLIS -> timestampWithMillisFormatter
        Format.SECS -> timestampWithSecsFormatter
    }

    companion object {
        // 00:00:36.150
        private const val TIME_PATTERN_MILLIS = "HH:mm:ss.SSS"

        // 00:00:36
        private const val TIME_PATTERN_SECS = "HH:mm:ss"
    }
}

