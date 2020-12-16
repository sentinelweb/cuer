package uk.co.sentinelweb.cuer.app.db.typeconverter

import androidx.room.TypeConverter
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialisePlaylistConfig
import uk.co.sentinelweb.cuer.domain.ext.serialise

class PlaylistConfigJsonTypeConverter {

    @TypeConverter
    fun toDb(mt: PlaylistDomain.PlaylistConfigDomain): String = mt.serialise()

    @TypeConverter
    fun fromDb(mt: String): PlaylistDomain.PlaylistConfigDomain? = deserialisePlaylistConfig(mt)
}