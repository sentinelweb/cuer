package uk.co.sentinelweb.cuer.db.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.db.factory.DatabaseFactory
import uk.co.sentinelweb.cuer.app.db.repository.*
import uk.co.sentinelweb.cuer.db.mapper.*
import uk.co.sentinelweb.cuer.db.repository.*
import uk.co.sentinelweb.cuer.db.update.MediaUpdateMapper

object DatabaseModule {

    private val dataBaseModule = module {
        single { DatabaseFactory().createDatabase(get()) }
    }

    private val mapperModule = module {
        factory { PlaylistMapper(get()) }
        factory { PlaylistItemMapper() }
        factory { MediaMapper(get()) }
        factory { ChannelMapper(get()) }
        factory { ImageMapper() }
        factory { MediaUpdateMapper() }
    }

    private val repoModule = module {
        single { SqldelightPlaylistDatabaseRepository(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
        single<PlaylistDatabaseRepository> { get<SqldelightPlaylistDatabaseRepository>() }
        single { SqldelightPlaylistItemDatabaseRepository(get(), get(), get(), get(), get(), get()) }
        single<PlaylistItemDatabaseRepository> { get<SqldelightPlaylistItemDatabaseRepository>() }
        single { SqldelightMediaDatabaseRepository(get(), get(), get(), get(), get(), get(), get()) }
        single<MediaDatabaseRepository> { get<SqldelightMediaDatabaseRepository>() }
        single { SqldelightChannelDatabaseRepository(get(), get(), get(), get(), get()) }
        single<ChannelDatabaseRepository> { get<SqldelightChannelDatabaseRepository>() }
        single { SqldelightImageDatabaseRepository(get(), get(), get(), get()) }
        single<ImageDatabaseRepository> { get<SqldelightImageDatabaseRepository>() }
    }

    val modules = listOf(dataBaseModule, mapperModule, repoModule)
}