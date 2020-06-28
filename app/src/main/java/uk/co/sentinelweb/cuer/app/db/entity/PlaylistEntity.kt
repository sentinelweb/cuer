package uk.co.sentinelweb.cuer.app.db.entity

import androidx.room.*
import uk.co.sentinelweb.cuer.app.db.AppDatabase.Companion.INITIAL_ID
import uk.co.sentinelweb.cuer.app.db.typeconverter.PlaylistConfigJsonTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.PlaylistModeTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.PlaylistTypeTypeConverter
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.SINGLE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.USER

@Entity(
    tableName = "playlist",
    indices = arrayOf(
        Index("flags"),
        Index("type")
    )
)
@TypeConverters(
    PlaylistModeTypeConverter::class,
    PlaylistTypeTypeConverter::class,
    PlaylistConfigJsonTypeConverter::class
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = INITIAL_ID,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "currentIndex")
    val currentIndex: Int = -1,

    @ColumnInfo(name = "mode")
    val mode: PlaylistDomain.PlaylistModeDomain = SINGLE,

    @ColumnInfo(name = "flags")
    val flags: Long = 0,

    @ColumnInfo(name = "type")
    val type: PlaylistDomain.PlaylistTypeDomain = USER,

    @Embedded(prefix = "thumb")
    val thumb: Image? = null,

    @Embedded(prefix = "image")
    val image: Image? = null,

    @ColumnInfo(name = "config_json")
    val config: PlaylistDomain.PlaylistConfigDomain = PlaylistDomain.PlaylistConfigDomain()
) {
    companion object {
        const val FLAG_STARRED = 1L
        const val FLAG_ARCHIVED = 2L
    }
}