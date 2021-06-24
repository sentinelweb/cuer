package uk.co.sentinelweb.cuer.app.db

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.entity.update.MediaUpdateMapper
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.db.mapper.*
import uk.co.sentinelweb.cuer.app.db.repository.*
import uk.co.sentinelweb.cuer.app.db.typeconverter.InstantTypeConverter

object DatabaseModule {
    val dbModule = module {
        single { get<RoomWrapper>().createDb() }
        single { RoomWrapper(androidApplication()) }

        single {
            RoomChannelDatabaseRepository(
                channelDao = get<AppDatabase>().channelDao(),
                channelMapper = get(),
                coProvider = get(),
                log = get(),
                database = get()
            )
        }
        factory<ChannelDatabaseRepository> { get<RoomChannelDatabaseRepository>() }

        single {
            RoomMediaDatabaseRepository(
                mediaDao = get<AppDatabase>().mediaDao(),
                mediaMapper = get(),
                coProvider = get(),
                log = get(),
                database = get(),
                mediaUpdateMapper = get(),
                channelDatabaseRepository = get()
            )
        }
        factory<MediaDatabaseRepository> { get<RoomMediaDatabaseRepository>() }

        single {
            RoomPlaylistDatabaseRepository(
                playlistDao = get<AppDatabase>().playlistDao(),
                playlistMapper = get(),
                playlistItemDao = get<AppDatabase>().playlistItemDao(),
                playlistItemMapper = get(),
                roomMediaRepository = get(),
                roomChannelRepository = get(),
                channelDao = get<AppDatabase>().channelDao(),
                coProvider = get(),
                log = get(),
                database = get()
            )
        }
        factory<PlaylistDatabaseRepository> { get<RoomPlaylistDatabaseRepository>() }

        single {
            RoomPlaylistItemDatabaseRepository(
                playlistItemDao = get<AppDatabase>().playlistItemDao(),
                playlistItemMapper = get(),
                roomMediaRepository = get(),
                coProvider = get(),
                log = get(),
                database = get()
            )
        }
        factory<PlaylistItemDatabaseRepository> { get<RoomPlaylistItemDatabaseRepository>() }

        single {
            DatabaseInitializer(
                ytInteractor = get(),
                roomMediaRepository = get(),
                roomPlaylistRepository = get(),
                roomPlaylistItemRepository = get(),
                timeProvider = get(),
                contextProvider = get(),
                log = get()
            )
        }
        factory { PlaylistMapper(get(), get(), get(), get()) }
        factory { PlaylistItemMapper(get()) }
        factory { MediaMapper(get(), get()) }
        factory { MediaUpdateMapper(get()) }
        factory { ImageMapper() }
        factory { ChannelMapper(get()) }
        factory { InstantTypeConverter() }
    }
}