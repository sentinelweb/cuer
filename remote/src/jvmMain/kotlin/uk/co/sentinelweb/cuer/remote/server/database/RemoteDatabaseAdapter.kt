package uk.co.sentinelweb.cuer.remote.server.database

import uk.co.sentinelweb.cuer.domain.Domain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface RemoteDatabaseAdapter {

    suspend fun getPlaylists(): List<PlaylistDomain>
    suspend fun getPlaylist(id: Long): PlaylistDomain?
    suspend fun getPlaylistItem(id: Long): PlaylistItemDomain?
    suspend fun scanUrl(url: String): Domain?
    suspend fun commitPlaylistItem(item: PlaylistItemDomain): PlaylistItemDomain
}