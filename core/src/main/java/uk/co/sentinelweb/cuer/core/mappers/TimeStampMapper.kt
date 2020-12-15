package uk.co.sentinelweb.cuer.core.mappers

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimeStampMapper {

    private val timeStampFormatter = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN)
    private val simpleTimeStampFormatter = DateTimeFormatter.ofPattern(SIMPLE_DATETIME_PATTERN)

    // 2020-04-06T16:00:16Z
    // 2020-04-06T16:00:16.0Z
    // 2020-04-06T16:00:16.00Z
    // 2020-04-06T16:00:16.000Z
    fun mapTimestamp(dateString: String) = LocalDateTime.parse(dateString, timeStampFormatter)

    fun mapTimestamp(date: LocalDateTime) = timeStampFormatter.format(date)

    fun mapDuration(duration: String) = Duration.parse(duration).toMillis()

    fun mapDateTimeSimple(date: LocalDateTime): String = simpleTimeStampFormatter.format(date)

    fun mapDateTimeSimple(dateString: String) = LocalDateTime.parse(dateString, simpleTimeStampFormatter)

    companion object {
        private const val TIMESTAMP_PATTERN = "uuuu-MM-dd'T'HH:mm:ss[.S[S][S]]'Z'"
        private const val SIMPLE_DATETIME_PATTERN = "uuuu-MM-dd_HH:mm:ss"
    }
}