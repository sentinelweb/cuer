package uk.co.sentinelweb.cuer.app.db.dao

import androidx.room.*
import uk.co.sentinelweb.cuer.app.db.entity.MediaAndChannel
import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity
import uk.co.sentinelweb.cuer.app.db.entity.update.MediaPositionUpdateEntity

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

    @Query("SELECT * FROM media WHERE flags & :flags == :flags")
    suspend fun loadAllByFlags(flags: Long): List<MediaAndChannel>

    @Query("SELECT flags FROM media WHERE id == :id")
    suspend fun getFlags(id: Long): Long

    @Update(entity = MediaPositionUpdateEntity::class, onConflict = OnConflictStrategy.REPLACE)
    fun updatePosition(it: MediaPositionUpdateEntity)

}
