package uk.co.sentinelweb.cuer.app.db.typeconverter

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime

class LocalDateTimeTypeConverter {

    @TypeConverter
    fun toDb(mt: LocalDateTime?): String = mt?.toString() ?: NULL_REPRESENTATION

    @TypeConverter
    fun fromDb(mt: String): LocalDateTime? = when (mt) {
        NULL_REPRESENTATION -> null
        else -> mt.let { LocalDateTime.parse(it) }
    }

    companion object {
        private val NULL_REPRESENTATION = "null"
    }
}