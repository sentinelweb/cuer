package uk.co.sentinelweb.cuer.app.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import uk.co.sentinelweb.cuer.app.db.typeconverter.InstantTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.LocalDateTimeTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.MediaTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.PlatformTypeConverter

@Entity(
    tableName = "playlist_item",
    indices = []// todo order
)
@TypeConverters(
    MediaTypeConverter::class,
    PlatformTypeConverter::class,
    InstantTypeConverter::class,
    LocalDateTimeTypeConverter::class
)
data class PlaylistItemEntity constructor(

    @PrimaryKey(autoGenerate = true)
    val id: Int,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "mediaId")
    val mediaId: Long,

    @ColumnInfo(name = "order")
    val order: Long,

    @ColumnInfo(name = "flags")
    val flags: Long = 0,

    @ColumnInfo(name = "playlistId")
    val playlistId: Int

) {
    companion object {
        const val FLAG_WATCHED = 1
        const val FLAG_ARCHIVED = 2
    }
}