package uk.co.sentinelweb.cuer.app.ui.common.dialog.playlist

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistSelectDialogModelCreator constructor(
    private val playlistRepo: PlaylistDatabaseRepository,
    private val res: ResourceWrapper
) {

    suspend fun loadPlaylists(
        block: (List<PlaylistDomain>) -> Unit
    ) {
        playlistRepo
            .loadList(null)
            .takeIf { it.isSuccessful }
            ?.data?.apply { block(this) }
    }

    fun mapPlaylistSelectionForDialog(
        all: List<PlaylistDomain>,
        selected: Set<PlaylistDomain>,
        multi: Boolean = false,
        itemClick: (Int, Boolean) -> Unit,
        confirm: (() -> Unit)? = null,
        dismiss: () -> Unit = {}
    ) = SelectDialogModel(
        type = DialogModel.Type.PLAYLIST,
        multi = multi,
        title = res.getString(R.string.playlist_dialog_title),
        items = all.map { playlist ->
            SelectDialogModel.Item(
                playlist.title,
                selected = (selected.find { sel -> playlist.title == sel.title } != null),
                selectable = true
            )
        }.plus(
            SelectDialogModel.Item(
                res.getString(R.string.playlist_dialog_add_playlist),
                selected = false,
                selectable = false
            )
        ),
        itemClick = itemClick,
        confirm = confirm,
        dismiss = dismiss
    )
}