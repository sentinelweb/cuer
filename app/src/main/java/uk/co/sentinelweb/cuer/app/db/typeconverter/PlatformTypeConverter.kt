package uk.co.sentinelweb.cuer.app.db.typeconverter

import androidx.room.TypeConverter
import uk.co.sentinelweb.cuer.domain.MediaDomain.PlatformDomain

class PlatformTypeConverter {

    @TypeConverter
    fun toDb(mt: PlatformDomain): String = mt.toString()

    @TypeConverter
    fun fromDb(mt: String): PlatformDomain = PlatformDomain.valueOf(mt)
}