package uk.co.sentinelweb.cuer.core.mappers

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter

actual class TimeFormatter {
    private val timeStampFormatterMillis = DateTimeFormatter.ofPattern(TIME_PATTERN_MILLIS)
    private val timeStampFormatterSecs = DateTimeFormatter.ofPattern(TIME_PATTERN_SECS)

    actual fun formatTime(time: LocalDateTime, format: Format): String =
        formatter(format).format(time.toJavaLocalDateTime()).strip00()

    actual fun formatTime(timeSecs: Float, format: Format): String =
        try {
            formatter(format).format(java.time.LocalTime.ofNanoOfDay((timeSecs * 1_000_000_000).toLong())).strip00()
        } catch (e: Exception) {
            "-"
        }

    actual fun formatNow(format: Format): String =
        formatter(format).format(java.time.LocalDateTime.now()).strip00()

    actual fun formatMillis(l: Long, format: Format): String =
        try {
            formatter(format).format(java.time.LocalTime.ofNanoOfDay(l * 1_000_000)).strip00()
        } catch (e: Exception) {
            "-"
        }

    actual fun formatFrom(time: LocalDateTime, format: Format): String =
        try {
            formatter(format)
                .format(java.time.LocalTime.now().minusNanos(time.toJavaLocalDateTime().toLocalTime().toNanoOfDay()))
                .strip00()
        } catch (e: Exception) {
            "-"
        }

    private fun formatter(format: Format) = when (format) {
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