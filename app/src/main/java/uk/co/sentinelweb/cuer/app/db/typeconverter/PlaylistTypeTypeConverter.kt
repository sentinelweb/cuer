package uk.co.sentinelweb.cuer.app.db.typeconverter

import androidx.room.TypeConverter
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistTypeTypeConverter {

    @TypeConverter
    fun toDb(mt: PlaylistDomain.PlaylistTypeDomain): String = mt.toString()

    @TypeConverter
    fun fromDb(mt: String): PlaylistDomain.PlaylistTypeDomain =
        PlaylistDomain.PlaylistTypeDomain.valueOf(mt)
}