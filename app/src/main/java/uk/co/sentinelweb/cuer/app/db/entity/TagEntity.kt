package uk.co.sentinelweb.cuer.app.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag")
data class TagEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "label") val label: String?
)
