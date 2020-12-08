package uk.co.sentinelweb.cuer.app.ui.playlists

import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

data class PlaylistsState constructor(
    var playlists: List<PlaylistDomain> = listOf(),
    var deletedPlaylist: PlaylistDomain? = null,
    var dragFrom: Int? = null,
    var dragTo: Int? = null
) : ViewModel()