package uk.co.sentinelweb.cuer.core.mappers

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime


expect class TimeStampMapper {
    fun mapTimestamp(dateString: String): LocalDateTime?
    fun mapTimestamp(date: LocalDateTime): String
    fun mapTimestamp(date: Instant): String
    fun mapDuration(duration: String): Long?
}