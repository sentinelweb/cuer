package uk.co.sentinelweb.cuer.app.db.dao

import androidx.room.*
import uk.co.sentinelweb.cuer.app.db.entity.ChannelEntity

@Dao
interface ChannelDao {
    @Transaction
    @Query("SELECT * FROM channel")
    fun getAll(): List<ChannelEntity>

    @Query("SELECT count() FROM channel")
    fun count(): Int

    @Transaction
    @Query("SELECT * FROM channel WHERE id IN (:channelIds)")
    fun loadAllByIds(channelIds: IntArray): List<ChannelEntity>

    @Transaction
    @Query("SELECT * FROM channel WHERE id == :id")
    fun load(id: Int): ChannelEntity?

    @Transaction
    @Query("SELECT * FROM channel WHERE remote_id LIKE :channelId LIMIT 1")
    fun findByChannelId(channelId: String): ChannelEntity?

    @Insert
    fun insertAll(channels: List<ChannelEntity>)

    @Delete
    fun delete(channel: ChannelEntity)

    @Query("DELETE FROM channel")
    fun deleteAll()

}
