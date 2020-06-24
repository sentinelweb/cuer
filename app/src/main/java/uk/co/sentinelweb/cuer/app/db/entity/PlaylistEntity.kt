package uk.co.sentinelweb.cuer.app.db.entity

import androidx.room.*
import uk.co.sentinelweb.cuer.app.db.typeconverter.*
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.SINGLE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.USER

@Entity(tableName = "playlist")
@TypeConverters(
    PlatformTypeConverter::class,
    InstantTypeConverter::class,
    LocalDateTimeTypeConverter::class,
    PlaylistModeTypeConverter::class,
    PlaylistTypeTypeConverter::class

)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    @ColumnInfo(name = "currentIndex")
    val currentIndex: Int = -1,

    @ColumnInfo(name = "mode")
    val mode: PlaylistDomain.PlaylistModeDomain = SINGLE,

    @ColumnInfo(name = "flags")
    val flags: Long = 0,

    @ColumnInfo(name = "type")
    val type: PlaylistDomain.PlaylistTypeDomain = USER,

    @ColumnInfo(name = "platform")
    val platform: PlatformDomain = YOUTUBE,

    @Embedded(prefix = "thumb")
    val thumb: Image? = null,

    @Embedded(prefix = "image")
    val image: Image? = null
) {
    companion object {
        const val FLAG_STARRED = 1
        const val FLAG_ARCHIVED = 2
    }
}