package uk.co.sentinelweb.cuer.app.db.di

import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.Database

object IosDatabaseModule {
    private val driverModule = module {
        single { NativeSqliteDriver(Database.Schema, "database.db") }
    }

    val modules = listOf(driverModule)
}