package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

data class PlaylistItemEditState constructor(
    var model: PlaylistItemEditModel? = null,
    var media: MediaDomain? = null,
    val selectedPlaylists: MutableSet<PlaylistDomain> = mutableSetOf(),
    var allPlaylists: List<PlaylistDomain>? = null
)