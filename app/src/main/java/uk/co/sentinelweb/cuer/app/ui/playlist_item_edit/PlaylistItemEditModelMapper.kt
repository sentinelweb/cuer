package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistItemEditModelMapper() {

    fun map(
        domain: MediaDomain,
        selectedPlaylists: MutableSet<PlaylistDomain>
    ) = PlaylistItemEditModel(
        title = domain.title,
        description = domain.description,
        imageUrl = (domain.image ?: domain.thumbNail)?.url,
        channelTitle = domain.channelData.title,
        channelThumbUrl = (domain.channelData.thumbNail ?: domain.channelData.image)?.url,
        chips = mutableListOf(ChipModel(ChipModel.Type.PLAYLIST_SELECT)).apply {
            selectedPlaylists.forEachIndexed { index, playlist ->
                add(index, ChipModel(ChipModel.Type.PLAYLIST, playlist.title, playlist.id))
            }
        },
        starred = domain.starred,
        canPlay = domain.mediaId.isNotEmpty()
    )

    fun mapSelection(all: List<PlaylistDomain>, selected: Set<PlaylistDomain>): SelectDialogModel =
        SelectDialogModel(
            type = SelectDialogModel.Type.PLAYLIST,
            title = "Select Playlist",
            items = all.map { playlist ->
                SelectDialogModel.Item(
                    playlist.title,
                    selected = (selected.find { sel -> playlist.title == sel.title } != null)
                )
            }
        )
}
