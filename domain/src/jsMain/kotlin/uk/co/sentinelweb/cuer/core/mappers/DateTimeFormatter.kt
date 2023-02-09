package uk.co.sentinelweb.cuer.core.mappers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

actual class DateTimeFormatter actual constructor() {
    actual fun formatDateTime(d: LocalDateTime): String {
        TODO("Not yet implemented")
    }

    actual fun formatDate(d: LocalDate): String {
        TODO("Not yet implemented")
    }

    actual fun formatDateTimeNullable(dateTime: LocalDateTime?): String {
        TODO("Not yet implemented")
    }
}