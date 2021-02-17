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

    //@Transaction
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

    @Query("SELECT * FROM playlist WHERE platform_id IN (:platformIds)")
    suspend fun loadAllByPlatformIds(platformIds: List<String>): List<PlaylistEntity>

    @Query("SELECT * FROM playlist WHERE flags & :flags == :flags")
    suspend fun loadAllByFlags(flags: Long): List<PlaylistEntity>

    //@Transaction
    @Query("SELECT * FROM playlist WHERE id == :id")
    suspend fun loadWithItems(id: Long): PlaylistAndItems?

    //@Transaction
    @Query("SELECT * FROM playlist WHERE id IN (:playlistIds)")
    suspend fun loadAllByIdsWithItems(playlistIds: LongArray): List<PlaylistAndItems>

    //@Transaction
    @Query("SELECT * FROM playlist WHERE platform_id IN (:platformIds)")
    suspend fun loadAllByPlatformIdsWithItems(platformIds: List<String>): List<PlaylistAndItems>

    //@Transaction
    @Query("SELECT * FROM playlist WHERE flags & :flags == :flags")
    suspend fun loadAllByFlagsWithItems(flags: Long): List<PlaylistAndItems>

    @Query("UPDATE playlist SET currentIndex=:index WHERE id=:id")
    suspend fun updateIndex(id: Long, index: Int): Int

    // todo can make partial updates entities for fields
    // https://stackoverflow.com/questions/45789325/update-some-specific-field-of-an-entity-in-android-room/59834309#59834309
    @Update
    suspend fun update(playlist: PlaylistEntity): Int

    @Delete
    suspend fun delete(playlist: PlaylistEntity)

    @Query("DELETE FROM playlist")
    suspend fun deleteAll()

    @Query("select distinct playlist.* from playlist, playlist_item, media, channel where playlist.id==playlist_item.playlist_id and playlist_item.media_id==media.id and media.channel_id==channel.id and channel.remote_id=:channelId")
    suspend fun findPlaylistsForChannePlatformlId(channelId: String): List<PlaylistEntity>
}