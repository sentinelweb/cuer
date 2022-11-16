package uk.co.sentinelweb.cuer.net.mappers

//import java.time.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

actual class TimeStampMapper constructor(
    private val log: LogWrapper
) {

    // 2020-04-06T16:00:16Z
    // 2020-04-06T16:00:16.0Z
    // 2020-04-06T16:00:16.00Z
    // 2020-04-06T16:00:16.000Z
    // 2020-04-06T16:00:16.0000Z
    // 2020-04-06T16:00:16.00000Z
    // 2020-04-06T16:00:16.000000Z
    actual fun parseTimestamp(dateString: String): LocalDateTime? = TODO()

    actual fun parseTimestampInstant(dateString: String): Instant? = TODO()

    actual fun toTimestamp(date: LocalDateTime): String = TODO()

    actual fun toTimestamp(date: Instant): String = TODO()

    actual fun parseDuration(duration: String): Long? = TODO()

    actual fun toTimestampSimple(date: LocalDateTime): String = TODO()

    actual fun toTimeStampSimple(ms: Long): String = TODO()

    actual fun nanosToLocalDateTime(ns: Long): LocalDateTime? = TODO()


    companion object {
        private const val TIMESTAMP_PATTERN = "uuuu-MM-dd'T'HH:mm:ss[.n]'Z'"
        private const val SIMPLE_DATETIME_PATTERN = "uuuu-MM-dd_HH:mm:ss"
    }
}