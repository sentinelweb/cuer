package uk.co.sentinelweb.cuer.app.db.entity

import androidx.room.Embedded
import androidx.room.Relation

class PlaylistItemAndMediaAndChannel(
    @Embedded val item: PlaylistItemEntity,
    @Relation(
        parentColumn = "media_id",
        entityColumn = "id",
        entity = MediaEntity::class
    )
    val media: MediaAndChannel
)