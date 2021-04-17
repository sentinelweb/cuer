package uk.co.sentinelweb.cuer.app.db.dao

import androidx.room.*
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistItemAndMediaAndChannel
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

    @Query("SELECT * FROM playlist_item")
    suspend fun loadAllItems(): List<PlaylistItemEntity>

    @Query("SELECT * FROM playlist_item  WHERE id IN (:playlistItemIds)")
    suspend fun loadAllByIds(playlistItemIds: List<Long>): List<PlaylistItemEntity>

    @Query("SELECT * FROM playlist_item WHERE media_id IN (:mediaIds)")
    suspend fun loadItemsByMediaId(mediaIds: List<Long>): List<PlaylistItemEntity>

    @Query("select count() from playlist_item,media where playlist_item.playlist_id=:playlistId and media.id=playlist_item.media_id and media.flags & :flags == :flags")
    suspend fun countMediaFlags(playlistId: Long, flags: Long): Int

    @Query("SELECT count() FROM playlist_item WHERE playlist_id ==:playlistId")
    suspend fun countItems(playlistId: Long): Int

    // todo try select distinct on plylist field (have to add te field explicitly)
    @Transaction
    @Query("SELECT playlist_item.* FROM playlist_item, media WHERE media.flags & 1 == 0 and media.id=playlist_item.media_id order by playlist_item.date_added desc LIMIT :limit")
    suspend fun loadAllPlaylistItemsWithNewMedia(limit: Int): List<PlaylistItemAndMediaAndChannel>

    @Transaction
    @Query("SELECT playlist_item.* FROM playlist_item, media WHERE media.flags & 1 == 1 and media.id = playlist_item.media_id and media.date_last_played != 'null' order by media.date_last_played desc LIMIT :limit")
    suspend fun loadAllPlaylistItemsRecent(limit: Int): List<PlaylistItemAndMediaAndChannel>

    @Transaction
    @Query("SELECT playlist_item.* FROM playlist_item, media WHERE INSTR(LOWER(media.title),:text) and media.id = playlist_item.media_id order by media.date_last_played desc LIMIT :limit")
    suspend fun search(text: String, limit: Int): List<PlaylistItemAndMediaAndChannel>

    @Transaction
    @Query("SELECT playlist_item.* FROM playlist_item, media WHERE INSTR(LOWER(media.title),:text) and playlist_item.playlist_id IN (:playlistIds) and media.id = playlist_item.media_id order by media.date_last_played desc LIMIT :limit")
    suspend fun search(text: String, playlistIds: List<Long>, limit: Int): List<PlaylistItemAndMediaAndChannel>
}