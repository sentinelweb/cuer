package uk.co.sentinelweb.cuer.app.db.typeconverter

import androidx.room.TypeConverter
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistModeTypeConverter {

    @TypeConverter
    fun toDb(mt: PlaylistDomain.PlaylistModeDomain): String = mt.toString()

    @TypeConverter
    fun fromDb(mt: String): PlaylistDomain.PlaylistModeDomain =
        PlaylistDomain.PlaylistModeDomain.valueOf(mt)
}