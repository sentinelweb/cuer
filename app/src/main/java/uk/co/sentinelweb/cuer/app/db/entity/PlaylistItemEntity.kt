package uk.co.sentinelweb.cuer.app.db.entity

import androidx.room.*
import uk.co.sentinelweb.cuer.app.db.typeconverter.InstantTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.LocalDateTimeTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.MediaTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.PlatformTypeConverter

@Entity(
    tableName = "playlist_item",
    indices = arrayOf(
        Index("order", "playlist_id", unique = true),
        Index("playlist_id"),
        Index("media_id"),
        Index("flags")
    )
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

    @ColumnInfo(name = "media_id")
    val mediaId: Int,

    @ColumnInfo(name = "order")
    val order: Long,

    @ColumnInfo(name = "flags")
    val flags: Long = 0,

    @ColumnInfo(name = "playlist_id")
    val playlistId: Int

) {
    companion object {
        const val FLAG_WATCHED = 1
        const val FLAG_ARCHIVED = 2
    }
}