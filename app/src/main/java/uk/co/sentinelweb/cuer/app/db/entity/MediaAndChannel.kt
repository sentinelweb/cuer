package uk.co.sentinelweb.cuer.app.db.entity

import androidx.room.Embedded
import androidx.room.Relation

class MediaAndChannel(
    @Embedded val media: MediaEntity,
    @Relation(
        parentColumn = "channel_id",
        entityColumn = "id",
        entity = ChannelEntity::class
    )
    val channel: ChannelEntity
)