package uk.co.sentinelweb.cuer.app.db.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.factory.DatabaseFactory
import uk.co.sentinelweb.cuer.app.db.mapper.ChannelMapper
import uk.co.sentinelweb.cuer.app.db.mapper.ImageMapper
import uk.co.sentinelweb.cuer.app.db.repository.ChannelDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.ImageDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.SqldelightChannelDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.SqldelightImageDatabaseRepository

object DatabaseModule {

    private val dataBaseModule = module {
        single { DatabaseFactory().createDatabase(get()) }
    }

    private val mapperModule = module {
        factory { ChannelMapper(get()) }
        factory { ImageMapper() }
    }

    private val repoModule = module {
        single<ChannelDatabaseRepository> { SqldelightChannelDatabaseRepository(get(), get(), get(), get(), get()) }
        single {SqldelightImageDatabaseRepository(get(), get(), get(), get())}
        single<ImageDatabaseRepository> { get<SqldelightImageDatabaseRepository>() }
    }

    val modules = listOf(dataBaseModule, mapperModule, repoModule)
}