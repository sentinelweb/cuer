package uk.co.sentinelweb.cuer.app.db.dao

import androidx.room.*
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistAndItems
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistEntity

@Dao
interface PlaylistDao {

    @Query("SELECT count() FROM playlist")
    suspend fun count(): Int

    @Query("SELECT count() FROM playlist")
    suspend fun countItems(): Int

    @Transaction
    @Query("SELECT * FROM playlist")
    suspend fun getAllPlaylistsWithItems(): List<PlaylistAndItems>

    @Query("SELECT * FROM playlist")
    suspend fun getAllPlaylists(): List<PlaylistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(playlists: List<PlaylistEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: PlaylistEntity): Long

    @Query("SELECT * FROM playlist WHERE id == :id")
    suspend fun load(id: Long): PlaylistEntity?

    @Query("SELECT * FROM playlist WHERE id IN (:playlistIds)")
    suspend fun loadAllByIds(playlistIds: LongArray): List<PlaylistEntity>

    @Query("SELECT * FROM playlist WHERE flags & :flags == :flags")
    suspend fun loadAllByFlags(flags: Long): List<PlaylistEntity>

    @Transaction
    @Query("SELECT * FROM playlist WHERE id == :id")
    suspend fun loadWithItems(id: Long): PlaylistAndItems?

    @Transaction
    @Query("SELECT * FROM playlist WHERE id IN (:playlistIds)")
    suspend fun loadAllByIdsWithItems(playlistIds: LongArray): List<PlaylistAndItems>

    @Transaction
    @Query("SELECT * FROM playlist WHERE flags & :flags == :flags")
    suspend fun loadAllByFlagsWithItems(flags: Long): List<PlaylistAndItems>

    @Update
    suspend fun update(playlist: PlaylistEntity)

    @Delete
    suspend fun delete(playlist: PlaylistEntity)

    @Query("DELETE FROM playlist")
    suspend fun deleteAll()
}