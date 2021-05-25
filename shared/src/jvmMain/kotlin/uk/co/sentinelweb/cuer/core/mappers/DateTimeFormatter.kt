package uk.co.sentinelweb.cuer.core.mappers

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.*

class DateTimeFormatter {
    private val patternDateTime: String = DateTimeFormatterBuilder
        .getLocalizedDateTimePattern(
            FormatStyle.SHORT, FormatStyle.SHORT, IsoChronology.INSTANCE, Locale.getDefault()
        )
    private val pubDateTimeFormatter = DateTimeFormatter.ofPattern(patternDateTime)

    private val patternDate: String = "dd/MM/yyyy"
    private val pubDateFormatter = DateTimeFormatter.ofPattern(patternDate)

    fun formatDateTime(d: LocalDateTime) = pubDateTimeFormatter.format(d)

    fun formatDate(d: LocalDate) = pubDateFormatter.format(d)

    fun formatDateTimeNullable(d: LocalDateTime?) = d.let { pubDateTimeFormatter.format(it) } ?: "-"
}