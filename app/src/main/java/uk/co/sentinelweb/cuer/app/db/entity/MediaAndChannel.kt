package uk.co.sentinelweb.cuer.app.db.entity

import androidx.room.Embedded
import androidx.room.Relation

class MediaAndChannel(
    @Relation(
        parentColumn = "id",
        entityColumn = "channelId"
    )
    val media: MediaEntity,
    @Embedded val channel: ChannelEntity
)