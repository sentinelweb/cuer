package uk.co.sentinelweb.cuer.app.db

import androidx.room.Database
import androidx.room.RoomDatabase
import uk.co.sentinelweb.cuer.app.db.dao.MediaDao
import uk.co.sentinelweb.cuer.app.db.dao.UserDao
import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity
import uk.co.sentinelweb.cuer.app.db.entity.TagEntity
import uk.co.sentinelweb.cuer.app.db.entity.UserEntity

@Database(version = 1, entities = arrayOf(
    UserEntity::class,
    MediaEntity::class,
    TagEntity::class
))
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun mediaDao(): MediaDao
}
