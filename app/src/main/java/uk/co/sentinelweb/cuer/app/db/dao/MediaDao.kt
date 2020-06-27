package uk.co.sentinelweb.cuer.app.db.dao

import androidx.room.*
import uk.co.sentinelweb.cuer.app.db.entity.MediaAndChannel
import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity

@Dao
interface MediaDao {
    @Transaction
    @Query("SELECT * FROM media")
    fun getAll(): List<MediaAndChannel>

    @Query("SELECT count() FROM media")
    fun count(): Int

    @Transaction
    @Query("SELECT * FROM media WHERE id IN (:mediaIds)")
    fun loadAllByIds(mediaIds: IntArray): List<MediaAndChannel>

    @Transaction
    @Query("SELECT * FROM media WHERE id == :id")
    fun load(id: Int): MediaAndChannel?

    @Transaction
    @Query("SELECT * FROM media WHERE media_id LIKE :mediaId LIMIT 1")
    fun findByMediaId(mediaId: String): MediaAndChannel?

    @Insert
    fun insertAll(medias: List<MediaEntity>)

    @Delete
    fun delete(media: MediaEntity)

    @Query("DELETE FROM media")
    fun deleteAll()

}
