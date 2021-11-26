package uk.co.sentinelweb.cuer.app.db.typeconverter

import androidx.room.TypeConverter
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain

class MediaTypeConverter {

    @TypeConverter
    fun toDb(mt: MediaTypeDomain): String = mt.toString()

    @TypeConverter
    fun fromDb(mt: String): MediaTypeDomain = MediaTypeDomain.valueOf(mt)
}