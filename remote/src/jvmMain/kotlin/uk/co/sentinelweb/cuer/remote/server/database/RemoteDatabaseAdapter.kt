package uk.co.sentinelweb.cuer.remote.server.database

import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface RemoteDatabaseAdapter {

    fun getPlaylists(): List<PlaylistDomain>
    fun getPlaylist(id: Long): PlaylistDomain?
    fun getPlaylistItem(id: Long): PlaylistItemDomain?

}