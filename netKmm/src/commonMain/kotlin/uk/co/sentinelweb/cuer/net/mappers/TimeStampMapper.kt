package uk.co.sentinelweb.cuer.net.mappers

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

//import java.time.format.DateTimeFormatter

// todo remove and make ktor converters
expect class TimeStampMapper {

//    private val timeStampFormatter = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN)
//    private val simpleTimeStampFormatter = DateTimeFormatter.ofPattern(SIMPLE_DATETIME_PATTERN)

    // 2020-04-06T16:00:16Z
    // 2020-04-06T16:00:16.0Z
    // 2020-04-06T16:00:16.00Z
    // 2020-04-06T16:00:16.000Z
    // 2020-04-06T16:00:16.0000Z
    // 2020-04-06T16:00:16.00000Z
    // 2020-04-06T16:00:16.000000Z
    fun mapTimestamp(dateString: String): LocalDateTime?

    fun mapTimestampInstant(dateString: String): Instant?

    fun mapTimestamp(date: LocalDateTime): String

    fun mapTimestamp(date: Instant): String

    fun mapDuration(duration: String): Long?

    fun mapDateTimeSimple(date: LocalDateTime): String

    fun mapDateTimeSimple(ms: Long): String

    fun mapDateTimeSimple(dateString: String): LocalDateTime?

    // fixme: looks like ofEpochSecond takes microseconds?
    //fun toLocalDateTime(ms: Long) : LocalDateTime

    fun toLocalDateTimeNano(ns: Long): LocalDateTime?

    fun toLocalDateNano(ns: Long): LocalDate

//    companion object {
//        internal const val TIMESTAMP_PATTERN = "uuuu-MM-dd'T'HH:mm:ss[.n]'Z'"
//        internal const val SIMPLE_DATETIME_PATTERN = "uuuu-MM-dd_HH:mm:ss"
//    }
}