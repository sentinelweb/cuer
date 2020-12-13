package uk.co.sentinelweb.cuer.app.ui.playlist

import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

data class PlaylistState constructor(
    var playlistId: Long? = null,
    var playlist: PlaylistDomain? = null,
    var deletedPlaylistItem: PlaylistItemDomain? = null,
    var focusIndex: Int? = null,
    var lastFocusIndex: Int? = null, // used for undo
    var dragFrom: Int? = null,
    var dragTo: Int? = null,
    var allPlaylists: List<PlaylistDomain>? = null,
    var selectedPlaylistItem: PlaylistItemDomain? = null
) : ViewModel()