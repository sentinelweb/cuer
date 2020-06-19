package uk.co.sentinelweb.cuer.app.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity

@Dao
interface MediaDao {
    @Query("SELECT * FROM media")
    fun getAll(): List<MediaEntity>

    @Query("SELECT count() FROM media")
    fun count(): Int

    @Query("SELECT * FROM media WHERE id IN (:mediaIds)")
    fun loadAllByIds(mediaIds: IntArray): List<MediaEntity>

    @Query("SELECT * FROM media WHERE id == :id")
    fun load(id: Int): MediaEntity?

    @Query("SELECT * FROM media WHERE media_id LIKE :mediaId LIMIT 1")
    fun findByMediaId(mediaId: String): MediaEntity?

    @Insert
    fun insertAll(medias: List<MediaEntity>)

    @Delete
    fun delete(media: MediaEntity)

    @Query("DELETE FROM media")
    fun deleteAll()

}
