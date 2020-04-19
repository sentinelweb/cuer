package uk.co.sentinelweb.cuer.app.db

import android.app.Application
import androidx.room.Room

class RoomWrapper(private val app:Application) {
    fun createDb() =
        Room.databaseBuilder(
            app,
            AppDatabase::class.java, "cuer_database"
        ).build()

}