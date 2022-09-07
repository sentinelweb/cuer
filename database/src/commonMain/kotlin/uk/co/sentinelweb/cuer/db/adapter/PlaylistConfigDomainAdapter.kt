package uk.co.sentinelweb.cuer.db.adapter

import com.squareup.sqldelight.ColumnAdapter
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialisePlaylistConfig
import uk.co.sentinelweb.cuer.domain.ext.serialise

class PlaylistConfigDomainAdapter: ColumnAdapter<PlaylistDomain.PlaylistConfigDomain, String> {
    override fun decode(databaseValue: String) = deserialisePlaylistConfig(databaseValue)
    override fun encode(value: PlaylistDomain.PlaylistConfigDomain) = value.serialise()
}