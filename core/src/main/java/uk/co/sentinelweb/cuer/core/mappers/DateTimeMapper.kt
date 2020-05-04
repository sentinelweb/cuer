package uk.co.sentinelweb.cuer.core.mappers

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimeMapper {

    // 2020-04-06T16:00:16Z
    // 2020-04-06T16:00:16.0Z
    // 2020-04-06T16:00:16.00Z
    // 2020-04-06T16:00:16.000Z
    fun mapTimestamp(dateString: String) = LocalDateTime.parse(
        dateString,
        DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN)
    )

    fun mapDuration(duration: String) = Duration.parse(duration).toMillis()

    companion object {
        private const val TIMESTAMP_PATTERN = "uuuu-MM-dd'T'HH:mm:ss[.S[S][S]]'Z'"
    }
}