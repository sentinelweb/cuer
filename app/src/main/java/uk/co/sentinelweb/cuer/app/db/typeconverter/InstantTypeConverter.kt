package uk.co.sentinelweb.cuer.app.db.typeconverter

import androidx.room.TypeConverter
import java.time.Instant

class InstantTypeConverter {

    @TypeConverter
    fun toDb(mt: Instant?): String = mt?.toString() ?: NULL_REPRESENTATION

    @TypeConverter
    fun fromDb(mt: String): Instant? = when (mt) {
        NULL_REPRESENTATION -> null
        else -> mt.let { Instant.parse(it) }
    }

    companion object {
        private val NULL_REPRESENTATION = "null"
    }
}