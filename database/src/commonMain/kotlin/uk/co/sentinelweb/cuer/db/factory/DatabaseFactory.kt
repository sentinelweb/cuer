package uk.co.sentinelweb.cuer.db.factory

import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.database.entity.Channel
import uk.co.sentinelweb.cuer.database.entity.Media
import uk.co.sentinelweb.cuer.database.entity.Playlist
import uk.co.sentinelweb.cuer.database.entity.Playlist_item
import uk.co.sentinelweb.cuer.db.adapter.InstantAdapter
import uk.co.sentinelweb.cuer.db.adapter.LocalDatetimeAdapter
import uk.co.sentinelweb.cuer.db.adapter.PlaylistConfigDomainAdapter

class DatabaseFactory(
    private val log: LogWrapper,
    private val prefs: MultiPlatformPreferencesWrapper
) {

    init {
        log.tag(this)
    }

    fun createDatabase(driver: SqlDriver): Database {
        return Database(
            driver,
            Channel.Adapter(
                platformAdapter = EnumColumnAdapter(),
                publishedAdapter = LocalDatetimeAdapter()
            ),
            Media.Adapter(
                typeAdapter = EnumColumnAdapter(),
                date_last_playedAdapter = InstantAdapter(),
                platformAdapter = EnumColumnAdapter(),
                publishedAdapter = LocalDatetimeAdapter(),
//                broadcast_dateAdapter = LocalDatetimeAdapter(),
            ),
            Playlist.Adapter(
                modeAdapter = EnumColumnAdapter(),
                typeAdapter = EnumColumnAdapter(),
                platformAdapter = EnumColumnAdapter(),
                config_jsonAdapter = PlaylistConfigDomainAdapter()
            ),
            Playlist_item.Adapter(
                date_addedAdapter = InstantAdapter()
            )
        ).apply {
            Database.Schema.migrate(
                driver = driver,
                oldVersion = prefs.dbVersion,
                newVersion = Database.Schema.version,
            )
        }.also {
            prefs.dbVersion = Database.Schema.version
        }.apply {
            log.d("Database.Schema.version ${Database.Schema.version}")
        }
    }
}