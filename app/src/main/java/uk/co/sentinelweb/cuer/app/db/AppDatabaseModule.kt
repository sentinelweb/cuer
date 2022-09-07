package uk.co.sentinelweb.cuer.app.db

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.db.init.JsonDatabaseInitializer

object AppDatabaseModule {
    val module = module {
        single<DatabaseInitializer> { JsonDatabaseInitializer(get(), get(), get(), get(), get()) }
    }
}