package uk.co.sentinelweb.cuer.app.db

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.entity.update.MediaUpdateMapper
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.db.mapper.*
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.typeconverter.InstantTypeConverter

object DatabaseModule {
    val dbModule = module {
        single { get<RoomWrapper>().createDb() }
        single { RoomWrapper(androidApplication()) }

        single {
            MediaDatabaseRepository(
                mediaDao = get<AppDatabase>().mediaDao(),
                mediaMapper = get(),
                channelDao = get<AppDatabase>().channelDao(),
                channelMapper = get(),
                coProvider = get(),
                log = get(),
                database = get(),
                mediaUpdateMapper = get()
            )
        }
        single {
            PlaylistDatabaseRepository(
                playlistDao = get<AppDatabase>().playlistDao(),
                playlistMapper = get(),
                playlistItemDao = get<AppDatabase>().playlistItemDao(),
                playlistItemMapper = get(),
                mediaRepository = get(),
//                mediaDao = get<AppDatabase>().mediaDao(),
                coProvider = get(),
                log = get(),
                database = get()
            )
        }
        single {
            DatabaseInitializer(
                ytInteractor = get(),
                mediaRepository = get(),
                playlistRepository = get(),
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