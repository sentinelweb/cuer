package uk.co.sentinelweb.cuer.core.mappers

import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class DateTimeMapper {

    private val timeStampFormatter = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN)

    private var durationTimeFormatter: SimpleDateFormat =
        SimpleDateFormat(DURATION_PATTERN, Locale.getDefault())
            .apply { timeZone = TimeZone.getTimeZone("GMT") }

    // 2020-04-06T16:00:16Z
    // 2020-04-06T16:00:16.0Z
    // 2020-04-06T16:00:16.00Z
    // 2020-04-06T16:00:16.000Z
    fun mapTimestamp(dateString: String) = LocalDateTime.parse(
        dateString,
        timeStampFormatter
    )

    fun mapDuration(duration: String) = Duration.parse(duration).toMillis()

    fun formatTime(ms: Long): String {
        return durationTimeFormatter.format(Date(ms))
    }

    companion object {
        private const val TIMESTAMP_PATTERN = "uuuu-MM-dd'T'HH:mm:ss[.S[S][S]]'Z'"
        private const val DURATION_PATTERN = "hh:mm:ss"
    }
}