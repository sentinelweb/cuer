package uk.co.sentinelweb.cuer.app.db

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.mapper.*
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository

object DatabaseModule {
    val dbModule = module {
        single {
            get<RoomWrapper>().createDb()
        }
        single { RoomWrapper(androidApplication()) }
        factory {
            MediaDatabaseRepository(
                mediaDao = get<AppDatabase>().mediaDao(),
                mediaMapper = get(),
                channelDao = get(),
                channelMapper = get(),
                coProvider = get(),
                log = get()
            )
        }
        factory {
            PlaylistDatabaseRepository(
                get<AppDatabase>().playlistDao(),
                get(),
                get<AppDatabase>().playlistItemDao(),
                get(),
                get<AppDatabase>().mediaDao(),
                get(),
                get()
            )
        }
        factory { PlaylistMapper(get(), get()) }
        factory { PlaylistItemMapper(get()) }
        factory { MediaMapper(get(), get()) }
        factory { ImageMapper() }
        factory { ChannelMapper(get()) }
    }
}