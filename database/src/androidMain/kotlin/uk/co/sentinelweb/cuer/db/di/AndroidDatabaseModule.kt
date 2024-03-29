package uk.co.sentinelweb.cuer.db.di

import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.Database

object AndroidDatabaseModule {
    private val driverModule = module {
        single<SqlDriver> { AndroidSqliteDriver(Database.Schema, androidContext(), "database.db") }
    }

    val modules = listOf(driverModule)
}