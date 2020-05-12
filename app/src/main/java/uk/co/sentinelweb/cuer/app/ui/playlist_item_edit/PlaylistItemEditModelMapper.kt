package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import uk.co.sentinelweb.cuer.domain.MediaDomain

class PlaylistItemEditModelMapper() {

    fun map(domain: MediaDomain) = PlaylistItemEditModel(
        title = domain.title,
        description = domain.description,
        author = domain.channelTitle,
        authorImgUrl = null, // todo get channel data,
        chips = listOf(),
        imageUrl = (domain.image ?: domain.thumbNail)?.url,
        starred = domain.starred,
        canPlay = domain.mediaId.isNotEmpty()
    )
}