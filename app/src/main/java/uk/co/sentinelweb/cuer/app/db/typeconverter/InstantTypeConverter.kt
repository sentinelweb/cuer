package uk.co.sentinelweb.cuer.app.db.typeconverter

import androidx.room.TypeConverter
import java.time.Instant

class InstantTypeConverter {

    @TypeConverter
    fun toDb(mt: Instant): String = mt.toString()

    @TypeConverter
    fun fromDb(mt: String): Instant = Instant.parse(mt)
}