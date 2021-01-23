package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

data class PlaylistItemEditState(
    var model: PlaylistItemEditModel? = null,
    var media: MediaDomain? = null,
    val selectedPlaylistIds: MutableSet<Long> = mutableSetOf(),
    var allPlaylists: List<PlaylistDomain> = listOf(),
    var committedItems: List<PlaylistItemDomain>? = null,
    var editingPlaylistItem: PlaylistItemDomain? = null,
    var playlistsChanged: Boolean = false,
    var mediaChanged: Boolean = false
)