package uk.co.sentinelweb.cuer.app.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PlaylistItemAndMedia(
    @Relation(
        parentColumn = "id",
        entityColumn = "mediaId"
    )
    val item: PlaylistItemEntity,
    @Embedded val media: MediaEntity
)