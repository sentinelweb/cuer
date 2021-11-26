package uk.co.sentinelweb.cuer.app.db.typeconverter

import androidx.room.TypeConverter
import uk.co.sentinelweb.cuer.domain.PlatformDomain

class PlatformTypeConverter {

    @TypeConverter
    fun toDb(mt: PlatformDomain?): String = mt?.toString() ?: NULL_REPRESENTATION

    @TypeConverter
    fun fromDb(mt: String): PlatformDomain? = when (mt) {
        NULL_REPRESENTATION -> null
        else -> mt.let { PlatformDomain.valueOf(it) }
    }

    companion object {
        private val NULL_REPRESENTATION = "null"
    }
}