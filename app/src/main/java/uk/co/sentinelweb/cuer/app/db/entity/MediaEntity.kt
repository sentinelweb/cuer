package uk.co.sentinelweb.cuer.app.db.entity

import androidx.room.*
import uk.co.sentinelweb.cuer.app.db.typeconverter.InstantTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.LocalDateTimeTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.MediaTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.PlatformTypeConverter
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import java.time.Instant
import java.time.LocalDateTime

@Entity(
    tableName = "media",
    indices = arrayOf(
        Index("media_id", unique = true),
        Index("type"),
        Index("title"),
        Index("description"),
        Index("channel_id"),
        Index("flags")
    )
)
@TypeConverters(
    MediaTypeConverter::class,
    PlatformTypeConverter::class,
    InstantTypeConverter::class,
    LocalDateTimeTypeConverter::class
)
data class MediaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "media_id")
    val mediaId: String,

    @ColumnInfo(name = "type")
    val mediaType: MediaTypeDomain,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "duration")
    val duration: Long?,

    @ColumnInfo(name = "positon")
    val positon: Long?,

    @ColumnInfo(name = "date_last_played")
    val dateLastPlayed: Instant?,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "platform")
    val platform: PlatformDomain,

    @ColumnInfo(name = "published")
    val published: LocalDateTime? = null,

    @ColumnInfo(name = "channel_id")
    val channelId: Long? = null,

    @Embedded(prefix = "thumb")
    val thumbNail: Image?,

    @Embedded(prefix = "image")
    val image: Image?,

    @ColumnInfo(name = "flags")
    val flags: Long = 0
) {
    companion object {
        const val FLAG_WATCHED = 1L
        const val FLAG_STARRED = 2L
    }
}
