package uk.co.sentinelweb.cuer.app.db.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.factory.DatabaseFactory

object DatabaseModule {

    private val dataBaseModule = module {
        single { DatabaseFactory().createDatabase(get()) }
    }

    val modules = listOf(dataBaseModule)
}