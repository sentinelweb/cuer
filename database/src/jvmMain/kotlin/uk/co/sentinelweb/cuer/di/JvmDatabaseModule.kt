package uk.co.sentinelweb.cuer.db.di

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.db.repository.file.ConfigDirectory
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper

object JvmDatabaseModule {
    private val driverModule = module {
        single<SqlDriver> {
            JdbcSqliteDriver(url = "jdbc:sqlite:${ConfigDirectory.Path}/cuer_database.db")//
                .apply {
                    if (get<MultiPlatformPreferencesWrapper>().dbInitialised.not()) {
                        Database.Schema.create(this)
                    }
                }
        }
    }

    val modules = listOf(driverModule)
}