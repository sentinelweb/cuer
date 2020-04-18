package uk.co.sentinelweb.cuer.app.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import uk.co.sentinelweb.cuer.app.db.typeconverter.InstantTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.MediaTypeConverter
import uk.co.sentinelweb.cuer.app.db.typeconverter.PlatformTypeConverter
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain.PlatformDomain
import java.time.Instant

@Entity(tableName = "media")
@TypeConverters(
    MediaTypeConverter::class,
    PlatformTypeConverter::class,
    InstantTypeConverter::class
)
data class MediaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "mediaId")
    val mediaId: String,

    @ColumnInfo(name = "type")
    val mediaType: MediaTypeDomain,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "duration")
    val duration: Long?,

    @ColumnInfo(name = "positon")
    val positon: Long?,

    @ColumnInfo(name = "dateLastPlayed")
    val dateLastPlayed: Instant?,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "platform")
    val platform: PlatformDomain
)
