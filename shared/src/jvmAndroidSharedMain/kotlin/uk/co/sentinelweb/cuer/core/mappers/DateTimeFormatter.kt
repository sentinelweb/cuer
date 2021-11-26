package uk.co.sentinelweb.cuer.core.mappers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.*

actual class DateTimeFormatter {
    private val patternDateTime: String = DateTimeFormatterBuilder
        .getLocalizedDateTimePattern(
            FormatStyle.SHORT, FormatStyle.SHORT, IsoChronology.INSTANCE, Locale.getDefault()
        )
    private val pubDateTimeFormatter = DateTimeFormatter.ofPattern(patternDateTime)

    private val patternDate: String = "dd/MM/yyyy"
    private val pubDateFormatter = DateTimeFormatter.ofPattern(patternDate)

    actual fun formatDateTime(d: LocalDateTime) = pubDateTimeFormatter.format(d.toJavaLocalDateTime())

    actual fun formatDate(d: LocalDate) = pubDateFormatter.format(d.toJavaLocalDate())

    actual fun formatDateTimeNullable(d: LocalDateTime?) = d?.let { pubDateTimeFormatter.format(it.toJavaLocalDateTime()) } ?: "-"

}