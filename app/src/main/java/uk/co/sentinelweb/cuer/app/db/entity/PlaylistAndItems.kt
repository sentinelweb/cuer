package uk.co.sentinelweb.cuer.app.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PlaylistAndItems(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "playlist_id"
    )
    val items: List<PlaylistItemEntity>
)