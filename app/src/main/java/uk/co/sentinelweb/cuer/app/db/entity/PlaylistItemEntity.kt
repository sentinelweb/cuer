package uk.co.sentinelweb.cuer.app.db.entity

import androidx.room.*
import uk.co.sentinelweb.cuer.app.db.AppDatabase.Companion.INITIAL_ID
import uk.co.sentinelweb.cuer.app.db.typeconverter.InstantTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.MediaTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.PlatformTypeConverter
import java.time.Instant

@Entity(
    tableName = "playlist_item",
    indices = arrayOf(
        Index("order", "playlist_id"),
        Index("playlist_id"),
        Index("media_id"),
        Index("flags")
    )
)
@TypeConverters(
    MediaTypeConverter::class,
    PlatformTypeConverter::class,
    InstantTypeConverter::class
)
data class PlaylistItemEntity constructor(

    @PrimaryKey(autoGenerate = true)
    val id: Long = INITIAL_ID,

    @ColumnInfo(name = "media_id")
    val mediaId: Long,

    @ColumnInfo(name = "order")
    val order: Long,

    @ColumnInfo(name = "flags")
    val flags: Long = 0,

    @ColumnInfo(name = "playlist_id")
    val playlistId: Long,

    @ColumnInfo(name = "date_added")
    val dateAdded: Instant

) {
    companion object {
        const val FLAG_ARCHIVED = 1L
    }
}