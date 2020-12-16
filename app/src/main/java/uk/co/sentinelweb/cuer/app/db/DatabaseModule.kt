package uk.co.sentinelweb.cuer.app.db

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.db.mapper.*
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository

object DatabaseModule {
    val dbModule = module {
        single { println("createdb");get<RoomWrapper>().createDb() }
        single { println("RoomWrapper");RoomWrapper(androidApplication()) }

        factory {
            println("MediaDatabaseRepository")
            MediaDatabaseRepository(
                mediaDao = get<AppDatabase>().mediaDao(),
                mediaMapper = get(),
                channelDao = get<AppDatabase>().channelDao(),
                channelMapper = get(),
                coProvider = get(),
                log = get(),
                database = get()
            )
        }
        factory {
            println("PlaylistDatabaseRepository")
            PlaylistDatabaseRepository(
                playlistDao = get<AppDatabase>().playlistDao(),
                playlistMapper = get(),
                playlistItemDao = get<AppDatabase>().playlistItemDao(),
                playlistItemMapper = get(),
                mediaDao = get<AppDatabase>().mediaDao(),
                coProvider = get(),
                log = get()
            )
        }
        factory {
            println("DatabaseInitializer")
            DatabaseInitializer(
                ytInteractor = get(),
                mediaRepository = get(),
                playlistRepository = get(),
                timeProvider = get(),
                contextProvider = get(),
                log = get()
            )
        }
        factory { println("PlaylistMapper"); PlaylistMapper(get(), get()) }
        factory { println("PlaylistItemMapper"); PlaylistItemMapper(get()) }
        factory { println("MediaMapper"); MediaMapper(get(), get()) }
        factory { println("ImageMapper"); ImageMapper() }
        factory { println("ChannelMapper"); ChannelMapper(get()) }
    }
}