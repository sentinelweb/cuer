package uk.co.sentinelweb.cuer.app.db

import androidx.room.Database
import androidx.room.RoomDatabase
import uk.co.sentinelweb.cuer.app.db.dao.ChannelDao
import uk.co.sentinelweb.cuer.app.db.dao.MediaDao
import uk.co.sentinelweb.cuer.app.db.dao.PlaylistDao
import uk.co.sentinelweb.cuer.app.db.dao.PlaylistItemDao
import uk.co.sentinelweb.cuer.app.db.entity.ChannelEntity
import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistEntity
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistItemEntity

@Database(version = 1, entities = arrayOf(
    PlaylistEntity::class,
    PlaylistItemEntity::class,
    MediaEntity::class,
    ChannelEntity::class
))
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun mediaDao(): MediaDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistItemDao(): PlaylistItemDao
}
