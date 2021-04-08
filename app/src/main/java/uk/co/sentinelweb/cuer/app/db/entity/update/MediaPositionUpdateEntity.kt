package uk.co.sentinelweb.cuer.app.db.entity.update

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import uk.co.sentinelweb.cuer.app.db.AppDatabase
import uk.co.sentinelweb.cuer.app.db.typeconverter.InstantTypeConverter
import java.time.Instant

@Entity
@TypeConverters(
    InstantTypeConverter::class
)
data class MediaPositionUpdateEntity(
    @PrimaryKey
    val id: Long = AppDatabase.INITIAL_ID,

    @ColumnInfo(name = "duration")
    val duration: Long?,

    @ColumnInfo(name = "positon")
    val positon: Long?,

    @ColumnInfo(name = "date_last_played")
    val dateLastPlayed: Instant?,

    @ColumnInfo(name = "flags")
    val flags: Long = 0
)