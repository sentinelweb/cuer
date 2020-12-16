package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlaylistModelMapper constructor(
    private val res: ResourceWrapper
) {

    fun map(domain: PlaylistDomain): PlaylistModel = PlaylistModel(
        domain.items.mapIndexed { index, item ->
            map(item, index)
        }
    )

    private fun map(it: PlaylistItemDomain, index: Int): PlaylistModel.PlaylistItemModel {
        val top = it.media.title ?: "No title"
        val bottom = it.media.url
        return PlaylistModel.PlaylistItemModel(
            it.id!!,
            index,
            bottom,
            it.media.mediaType,
            top,
            it.media.duration?.let { "${it / 1000}s" } ?: "-",
            it.media.positon?.let { "${it / 1000}s" } ?: "-",
            it.media.thumbNail?.url
        )
    }

    fun mapChangePlaylistAlert(confirm: () -> Unit): AlertDialogModel = AlertDialogModel(
        res.getString(R.string.playlist_change_dialog_title),
        res.getString(R.string.playlist_change_dialog_message),
        confirm
    )

}
