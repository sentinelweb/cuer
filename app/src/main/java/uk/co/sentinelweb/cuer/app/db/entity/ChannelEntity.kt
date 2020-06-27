package uk.co.sentinelweb.cuer.app.db.entity

import androidx.room.*
import uk.co.sentinelweb.cuer.app.db.typeconverter.InstantTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.LocalDateTimeTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.PlatformTypeConverter
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import java.time.LocalDateTime

@Entity(
    tableName = "channel",
    indices = arrayOf(Index("remote_id", unique = true))
)
@TypeConverters(
    PlatformTypeConverter::class,
    InstantTypeConverter::class,
    LocalDateTimeTypeConverter::class
)
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(name = "remote_id")
    val remoteId: String,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "custom_url")
    val customUrl: String?,

    @ColumnInfo(name = "country")
    val country: String?,

    @ColumnInfo(name = "platform")
    val platform: PlatformDomain,

    @Embedded(prefix = "thumb")
    val thumbNail: Image?,

    @Embedded(prefix = "image")
    val image: Image?,

    @ColumnInfo(name = "flags")
    val flags: Long = 0,

    @ColumnInfo(name = "published")
    val published: LocalDateTime? = null
) {
    companion object {
        const val FLAG_STARRED = 1L
    }
}