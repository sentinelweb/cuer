package uk.co.sentinelweb.cuer.app.db.factory

import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import uk.co.sentinelweb.cuer.app.db.*
import uk.co.sentinelweb.cuer.app.db.adapter.InstantAdapter
import uk.co.sentinelweb.cuer.app.db.adapter.LocalDatetimeAdapter
import uk.co.sentinelweb.cuer.app.db.adapter.PlaylistConfigDomainAdapter

class DatabaseFactory {

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
                publishedAdapter = LocalDatetimeAdapter()
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
        )
    }
}