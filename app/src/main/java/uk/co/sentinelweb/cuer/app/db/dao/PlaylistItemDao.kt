package uk.co.sentinelweb.cuer.app.db.dao

import androidx.room.Dao
import androidx.room.Insert
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistItemEntity

@Dao
interface PlaylistItemDao {
    @Insert
    fun insertAll(medias: List<PlaylistItemEntity>)

    @Insert
    fun insert(playlistItem: PlaylistItemEntity)
}