package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistEditModelMapper constructor(
    private val res: ResourceWrapper,
    private val validator: PlaylistValidator
) {
    fun mapModel(domain: PlaylistDomain) = PlaylistEditModel(
        titleDisplay = if (domain.title.isBlank()) res.getString(R.string.pe_default_display_title) else domain.title,
        titleEdit = domain.title,
        starred = domain.starred,
        imageUrl = domain.image?.url,
        thumbUrl = domain.thumb?.url,
        button = res.getString(domain.id?.let { R.string.pe_save } ?: R.string.pe_create),
        validation = validator.validate(domain)
    )

}