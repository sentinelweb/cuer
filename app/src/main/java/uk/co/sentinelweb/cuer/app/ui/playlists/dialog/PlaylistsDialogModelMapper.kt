package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsContract

class PlaylistsDialogModelMapper() {

    fun map(
        playlistsModel: PlaylistsContract.Model?,
        config: PlaylistsDialogContract.Config,
        pinOn: Boolean
    ) = PlaylistsDialogContract.Model(
        playlistsModel,
        config.showAdd,
        config.showPin && pinOn,
        config.showPin && !pinOn
    )
}