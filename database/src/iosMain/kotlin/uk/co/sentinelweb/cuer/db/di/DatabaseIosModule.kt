package uk.co.sentinelweb.cuer.db.di

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.Database

object DatabaseIosModule {
    private val driverModule = module {
        single<SqlDriver> { NativeSqliteDriver(Database.Schema, "database.db") }
    }

    val modules = listOf(driverModule)
}