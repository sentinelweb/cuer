package uk.co.sentinelweb.cuer.core.mappers

import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TimeFormatter {
    private val timeStampFormatterMillis = DateTimeFormatter.ofPattern(TIME_PATTERN_MILLIS)
    private val timeStampFormatterSecs = DateTimeFormatter.ofPattern(TIME_PATTERN_SECS)

    enum class Format { SECS, MILLIS }

    fun formatTime(date: LocalTime, format: Format = Format.SECS): String =
        formatter().format(date).strip00()

    fun formatTime(timeSecs: Float, format: Format = Format.SECS): String =
        try {
            formatter().format(LocalTime.ofNanoOfDay((timeSecs * 1_000_000_000).toLong())).strip00()
        } catch (e: Exception) {
            "-"
        }

    fun formatNow(format: Format = Format.SECS): String =
        formatter().format(LocalTime.now()).strip00()

    fun formatMillis(l: Long, format: Format = Format.MILLIS): String =
        try {
            formatter().format(LocalTime.ofNanoOfDay(l * 1_000_000)).strip00()
        } catch (e: Exception) {
            "-"
        }

    fun formatFrom(time: LocalTime, format: Format = Format.SECS): String =
        try {
            formatter().format(LocalTime.now().minusNanos(time.toNanoOfDay())).strip00()
        } catch (e: Exception) {
            "-"
        }

    private fun formatter(format: Format = Format.SECS) = when (format) {
        Format.MILLIS -> timeStampFormatterMillis
        Format.SECS -> timeStampFormatterSecs
    }

    private fun String.strip00() = this.let {
        var formatted = it
        while (formatted.startsWith("00:")) {
            formatted = formatted.substring(3)
        }
        formatted
    }

    companion object {
        // 00:00:36.150
        private const val TIME_PATTERN_MILLIS = "HH:mm:ss.SSS"

        // 00:00:36
        private const val TIME_PATTERN_SECS = "HH:mm:ss"
    }

}