package uk.co.sentinelweb.cuer.app.db.entity

import androidx.room.*
import uk.co.sentinelweb.cuer.app.db.typeconverter.InstantTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.LocalDateTimeTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.MediaTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.PlatformTypeConverter
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain.PlatformDomain
import java.time.Instant
import java.time.LocalDateTime

@Entity(tableName = "media")
@TypeConverters(
    MediaTypeConverter::class,
    PlatformTypeConverter::class,
    InstantTypeConverter::class,
    LocalDateTimeTypeConverter::class
)
data class MediaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

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
    val channelId: String? = null,

    @ColumnInfo(name = "channel_title")
    val channelTitle: String? = null,

    @Embedded(prefix =  "thumb")
    val thumbNail: Image?,

    @Embedded(prefix =  "image")
    val image: Image?
) {
    data class Image constructor(
        val url: String,
        val width: Int?,
        val height: Int?
    )
}
