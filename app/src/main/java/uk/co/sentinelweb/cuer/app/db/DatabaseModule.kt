package uk.co.sentinelweb.cuer.app.db

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.mapper.MediaMapper
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository

object DatabaseModule {
    val dbModule = module {
        single {
            get<RoomWrapper>().createDb()
        }
        single { RoomWrapper(androidApplication()) }
        factory { MediaDatabaseRepository(get<AppDatabase>().mediaDao(), get(), get(), get()) }
        factory { MediaMapper() }
    }
}