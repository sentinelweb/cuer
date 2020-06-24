package uk.co.sentinelweb.cuer.app.db.entity

import androidx.room.*
import uk.co.sentinelweb.cuer.app.db.typeconverter.InstantTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.LocalDateTimeTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.MediaTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.PlatformTypeConverter
import java.time.LocalDateTime

@Entity(tableName = "channel")
@TypeConverters(
    MediaTypeConverter::class,
    PlatformTypeConverter::class,
    InstantTypeConverter::class,
    LocalDateTimeTypeConverter::class
)
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "channel_id")
    val channelId: String,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "custom_url")
    val customUrl: String?,

    @ColumnInfo(name = "country")
    val country: String,

    @Embedded(prefix = "thumb")
    val thumbNail: Image?,

    @Embedded(prefix = "image")
    val image: Image?,

    @ColumnInfo(name = "flags")
    val flags: Long = 0,

    @ColumnInfo(name = "published")
    val published: LocalDateTime? = null
)