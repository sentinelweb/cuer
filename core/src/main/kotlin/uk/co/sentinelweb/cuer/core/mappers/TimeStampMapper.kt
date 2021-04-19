package uk.co.sentinelweb.cuer.core.mappers

import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class TimeStampMapper constructor(
    private val log: LogWrapper
) {

    private val timeStampFormatter = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN)
    private val simpleTimeStampFormatter = DateTimeFormatter.ofPattern(SIMPLE_DATETIME_PATTERN)

    // 2020-04-06T16:00:16Z
    // 2020-04-06T16:00:16.0Z
    // 2020-04-06T16:00:16.00Z
    // 2020-04-06T16:00:16.000Z
    // 2020-04-06T16:00:16.0000Z
    // 2020-04-06T16:00:16.00000Z
    // 2020-04-06T16:00:16.000000Z
    fun mapTimestamp(dateString: String): LocalDateTime? = try {
        LocalDateTime.parse(dateString, timeStampFormatter)
    } catch (e: Exception) {
        log.e("mapTimestamp could not parse : $dateString", e)
        null
    }

    fun mapTimestamp(date: LocalDateTime) = timeStampFormatter.format(date)

    fun mapTimestamp(date: Instant) = timeStampFormatter.format(date)

    fun mapDuration(duration: String) = try {
        Duration.parse(duration).toMillis()
    } catch (e: Exception) {
        log.e("mapDuration.could not parse : $duration", e)
        null
    }

    fun mapDateTimeSimple(date: LocalDateTime): String = simpleTimeStampFormatter.format(date)

    fun mapDateTimeSimple(ms: Long): String = simpleTimeStampFormatter.format(localDateTime(ms))

    fun mapDateTimeSimple(dateString: String): LocalDateTime? = try {
        LocalDateTime.parse(dateString, simpleTimeStampFormatter)
    } catch (e: Exception) {
        log.e("mapDateTimeSimple.could not parse : $dateString", e)
        null
    }

    private fun localDateTime(ms: Long) = LocalDateTime.ofEpochSecond(ms * 1000, 0, OffsetDateTime.now().getOffset())

    companion object {
        private const val TIMESTAMP_PATTERN = "uuuu-MM-dd'T'HH:mm:ss[.n]'Z'"
        private const val SIMPLE_DATETIME_PATTERN = "uuuu-MM-dd_HH:mm:ss"
    }
}