package uk.co.sentinelweb.cuer.net.mappers

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
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
    actual fun mapTimestamp(dateString: String): LocalDateTime? = TODO()

    actual fun mapTimestampInstant(dateString: String): Instant? = TODO()

    actual fun mapTimestamp(date: LocalDateTime): String = TODO()

    actual fun mapTimestamp(date: Instant): String = TODO()

    actual fun mapDuration(duration: String): Long? = TODO()

    actual fun mapDateTimeSimple(date: LocalDateTime): String = TODO()

    actual fun mapDateTimeSimple(ms: Long): String = TODO()

    actual fun mapDateTimeSimple(dateString: String): LocalDateTime? = TODO()

    actual fun toLocalDateTimeNano(ns: Long): LocalDateTime? = TODO()

    actual fun toLocalDateNano(ns: Long): LocalDate = TODO()

    companion object {
        private const val TIMESTAMP_PATTERN = "uuuu-MM-dd'T'HH:mm:ss[.n]'Z'"
        private const val SIMPLE_DATETIME_PATTERN = "uuuu-MM-dd_HH:mm:ss"
    }
}