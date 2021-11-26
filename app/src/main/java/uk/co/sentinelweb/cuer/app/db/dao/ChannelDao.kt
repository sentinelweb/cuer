package uk.co.sentinelweb.cuer.app.db.dao

import androidx.room.*
import uk.co.sentinelweb.cuer.app.db.entity.ChannelEntity

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channel")
    suspend fun getAll(): List<ChannelEntity>

    @Query("SELECT count() FROM channel")
    suspend fun count(): Int

    @Query("SELECT * FROM channel WHERE id IN (:channelIds)")
    suspend fun loadAllByIds(channelIds: IntArray): List<ChannelEntity>

    @Query("SELECT * FROM channel WHERE id == :id")
    suspend fun load(id: Long): ChannelEntity?

    @Query("SELECT * FROM channel WHERE remote_id LIKE :channelId LIMIT 1")
    suspend fun findByChannelId(channelId: String): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<ChannelEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(channel: ChannelEntity): Long

    @Update
    suspend fun update(channel: ChannelEntity)

    @Delete
    suspend fun delete(channel: ChannelEntity)

    @Query("DELETE FROM channel")
    suspend fun deleteAll()

}
