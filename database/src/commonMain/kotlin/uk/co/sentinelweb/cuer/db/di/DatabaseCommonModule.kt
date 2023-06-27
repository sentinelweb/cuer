package uk.co.sentinelweb.cuer.db.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.repository.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.db.factory.DatabaseFactory
import uk.co.sentinelweb.cuer.db.mapper.*
import uk.co.sentinelweb.cuer.db.repository.*
import uk.co.sentinelweb.cuer.db.update.MediaUpdateMapper

object DatabaseCommonModule {

    private val dataBaseModule = module {
        single { DatabaseFactory(get(), get()).createDatabase(get()) }
    }

    private val mapperModule = module {
        val source = OrchestratorContract.Source.LOCAL // todo pass param
        factory { PlaylistMapper(get(), source) }
        factory { PlaylistItemMapper(source) }
        factory { MediaMapper(get(), source) }
        factory { ChannelMapper(get(), source) }
        factory { ImageMapper(source) }
        factory { MediaUpdateMapper() }
    }

    private val repoModule = module {
        val source = OrchestratorContract.Source.LOCAL // todo pass param
        single { SqldelightPlaylistDatabaseRepository(get(), get(), get(), get(), get(), get(), get(), get(), source) }
        single<PlaylistDatabaseRepository> { get<SqldelightPlaylistDatabaseRepository>() }
        single { SqldelightPlaylistItemDatabaseRepository(get(), get(), get(), get(), get(), get(), source) }
        single<PlaylistItemDatabaseRepository> { get<SqldelightPlaylistItemDatabaseRepository>() }
        single { SqldelightMediaDatabaseRepository(get(), get(), get(), get(), get(), get(), get(), get(), source) }
        single<MediaDatabaseRepository> { get<SqldelightMediaDatabaseRepository>() }
        single { SqldelightChannelDatabaseRepository(get(), get(), get(), get(), get(), get(), source) }
        single<ChannelDatabaseRepository> { get<SqldelightChannelDatabaseRepository>() }
        single { SqldelightImageDatabaseRepository(get(), get(), get(), get(), get(), source) }
        single<ImageDatabaseRepository> { get<SqldelightImageDatabaseRepository>() }
    }

    val modules = listOf(dataBaseModule, mapperModule, repoModule)
}