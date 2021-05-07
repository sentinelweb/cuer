package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsContract

class PlaylistsDialogModelMapper() {

    fun map(
        playlistsModel: PlaylistsContract.Model,
        showAdd: Boolean,
        pinNext: Boolean
    ) = PlaylistsDialogContract.Model(
        playlistsModel,
        showAdd,
        pinNext
    )
}