package uk.co.sentinelweb.cuer.net.mappers

//import java.time.*
import kotlinx.datetime.*
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

actual class TimeStampMapper constructor(
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
    actual fun mapTimestamp(dateString: String): LocalDateTime? = try {
        java.time.LocalDateTime.parse(dateString, timeStampFormatter).toKotlinLocalDateTime()
    } catch (e: Exception) {
        log.e("mapTimestamp could not parse : $dateString", e)
        null
    }

    actual fun mapTimestampInstant(dateString: String): Instant? = try {
        Instant.parse(dateString)
    } catch (e: Exception) {
        log.e("mapTimestamp could not parse : $dateString", e)
        null
    }

    actual fun mapTimestamp(date: LocalDateTime): String = timeStampFormatter.format(date.toJavaLocalDateTime())

    actual fun mapTimestamp(date: Instant): String = timeStampFormatter.format(date.toJavaInstant())

    actual fun mapDuration(duration: String): Long? = try {
        Duration.parse(duration).inWholeMilliseconds
    } catch (e: Exception) {
        log.e("mapDuration.could not parse : $duration", e)
        null
    }

    actual fun mapDateTimeSimple(date: LocalDateTime): String =
        simpleTimeStampFormatter.format(date.toJavaLocalDateTime())

    actual fun mapDateTimeSimple(ms: Long): String = simpleTimeStampFormatter.format(toLocalDateTime(ms))

    actual fun mapDateTimeSimple(dateString: String): LocalDateTime? = try {
        java.time.LocalDateTime.parse(dateString, simpleTimeStampFormatter).toKotlinLocalDateTime()
    } catch (e: Exception) {
        log.e("mapDateTimeSimple.could not parse : $dateString", e)
        null
    }

    // fixme: looks like ofEpochSecond takes microseconds?
    private fun toLocalDateTime(ms: Long): java.time.LocalDateTime =
        java.time.LocalDateTime.ofEpochSecond(ms * 1000, 0, OffsetDateTime.now().getOffset())

    actual fun toLocalDateTimeNano(ns: Long): LocalDateTime? =
        java.time.LocalDateTime.ofEpochSecond(ns / 1000, 0, OffsetDateTime.now().getOffset()).toKotlinLocalDateTime()

    actual fun toLocalDateNano(ns: Long): LocalDate =
        java.time.LocalDate.ofEpochDay(ns / 1_000_000_000).toKotlinLocalDate()

    companion object {
        private const val TIMESTAMP_PATTERN = "uuuu-MM-dd'T'HH:mm:ss[.n]'Z'"
        private const val SIMPLE_DATETIME_PATTERN = "uuuu-MM-dd_HH:mm:ss"
    }
}