package uk.co.sentinelweb.cuer.app.db.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.factory.DatabaseFactory
import uk.co.sentinelweb.cuer.app.db.mapper.ChannelMapper
import uk.co.sentinelweb.cuer.app.db.mapper.ImageMapper
import uk.co.sentinelweb.cuer.app.db.mapper.MediaMapper
import uk.co.sentinelweb.cuer.app.db.repository.*

object DatabaseModule {

    private val dataBaseModule = module {
        single { DatabaseFactory().createDatabase(get()) }
    }

    private val mapperModule = module {
        factory { MediaMapper(get()) }
        factory { ChannelMapper(get()) }
        factory { ImageMapper() }
    }

    private val repoModule = module {
        single { SqldelightMediaDatabaseRepository(get(), get(), get(), get(), get(), get()) }
        single<MediaDatabaseRepository> { get<SqldelightMediaDatabaseRepository>() }
        single { SqldelightChannelDatabaseRepository(get(), get(), get(), get(), get()) }
        single<ChannelDatabaseRepository> { get<SqldelightChannelDatabaseRepository>() }
        single {SqldelightImageDatabaseRepository(get(), get(), get(), get())}
        single<ImageDatabaseRepository> { get<SqldelightImageDatabaseRepository>() }
    }

    val modules = listOf(dataBaseModule, mapperModule, repoModule)
}