package uk.co.sentinelweb.cuer.app.db.dao

import androidx.room.*
import uk.co.sentinelweb.cuer.app.db.entity.MediaAndChannel
import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity

@Dao
interface MediaDao {
    @Transaction
    @Query("SELECT * FROM media")
    suspend fun getAll(): List<MediaAndChannel>

    @Query("SELECT count() FROM media")
    suspend fun count(): Int

    //Transaction
    @Query("SELECT * FROM media WHERE id IN (:mediaIds)")
    suspend fun loadAllByIds(mediaIds: LongArray): List<MediaAndChannel>

    //@Transaction
    @Query("SELECT * FROM media WHERE id == :id")
    suspend fun load(id: Long): MediaAndChannel?

    //@Transaction
    @Query("SELECT * FROM media WHERE media_id LIKE :mediaId LIMIT 1")
    suspend fun findByMediaId(mediaId: String): MediaAndChannel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medias: List<MediaEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: MediaEntity): Long

    @Update
    suspend fun update(media: MediaEntity)

    @Delete
    suspend fun delete(media: MediaEntity)

    @Query("DELETE FROM media")
    suspend fun deleteAll()

}
