package uk.co.sentinelweb.cuer.app.db.typeconverter

import androidx.room.TypeConverter
import java.time.Instant

class InstantTypeConverter {

    @TypeConverter
    fun toDb(mt: Instant?): String = mt?.toString() ?: "null"

    @TypeConverter
    fun fromDb(mt: String): Instant? = when (mt) {
        "null" -> null
        else -> mt.let { Instant.parse(it) }
    }
}