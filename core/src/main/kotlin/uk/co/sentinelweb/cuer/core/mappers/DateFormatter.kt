package uk.co.sentinelweb.cuer.core.mappers

import java.time.LocalDateTime
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.*

class DateFormatter {
    private val pattern: String = DateTimeFormatterBuilder
        .getLocalizedDateTimePattern(
            FormatStyle.SHORT, FormatStyle.SHORT, IsoChronology.INSTANCE, Locale.getDefault()
        )
    private val pubDateFormatter = DateTimeFormatter.ofPattern(pattern)

    fun formatDate(d: LocalDateTime) = pubDateFormatter.format(d)

    fun formatDateNullable(d: LocalDateTime?) = d.let { pubDateFormatter.format(it) } ?: "-"
}