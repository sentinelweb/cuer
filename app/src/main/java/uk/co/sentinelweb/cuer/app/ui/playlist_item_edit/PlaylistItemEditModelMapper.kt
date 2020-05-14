package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import uk.co.sentinelweb.cuer.domain.MediaDomain

class PlaylistItemEditModelMapper() {

    fun map(domain: MediaDomain) = PlaylistItemEditModel(
        title = domain.title,
        description = domain.description,
        imageUrl = (domain.image ?: domain.thumbNail)?.url,
        channelTitle = domain.channelData.title,
        channelThumbUrl = (domain.channelData.thumbNail ?: domain.channelData.image)?.url,
        starred = domain.starred,
        canPlay = domain.mediaId.isNotEmpty()
    )
}