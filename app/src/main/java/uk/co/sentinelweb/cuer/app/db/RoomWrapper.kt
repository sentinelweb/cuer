package uk.co.sentinelweb.cuer.app.db

import android.app.Application
import androidx.room.Room

class RoomWrapper(private val app:Application) {
    fun createDb() =
        Room.databaseBuilder(
            app,
            AppDatabase::class.java, "cuer_database"
        )
//        .addCallback(object : RoomDatabase.Callback() {
//            override fun onCreate(db: SupportSQLiteDatabase) {
//                super.onCreate(db)
//            }
//
//            override fun onOpen(db: SupportSQLiteDatabase) {
//                super.onOpen(db)
//            }
//
//            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
//                super.onDestructiveMigration(db)
//            }
//        })
            .build()

}