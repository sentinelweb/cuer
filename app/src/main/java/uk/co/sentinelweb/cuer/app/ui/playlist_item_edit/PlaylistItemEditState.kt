package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import uk.co.sentinelweb.cuer.domain.MediaDomain

data class PlaylistItemEditState constructor(
    var model: PlaylistItemEditModel? = null,
    var media: MediaDomain? = null
)