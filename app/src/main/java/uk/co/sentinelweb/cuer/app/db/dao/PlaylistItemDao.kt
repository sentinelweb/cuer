package uk.co.sentinelweb.cuer.app.db.dao

import androidx.room.*
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistItemEntity

@Dao
interface PlaylistItemDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(playlistItems: List<PlaylistItemEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlistItem: PlaylistItemEntity): Long

    @Query("SELECT * FROM playlist_item WHERE id == :id")
    suspend fun load(id: Long): PlaylistItemEntity?

    @Update
    suspend fun update(playlist: PlaylistItemEntity)

    @Delete
    suspend fun delete(playlistItem: PlaylistItemEntity)

    @Query("DELETE FROM playlist_item WHERE playlist_id = :playlistId")
    suspend fun deletePlaylistItems(playlistId: Long)

    @Query("DELETE FROM playlist_item")
    suspend fun deleteAll()

    @Query("SELECT * FROM playlist_item WHERE media_id IN (:mediaIds)")
    fun loadItemsByMediaId(mediaIds: List<Long>): List<PlaylistItemEntity>
}