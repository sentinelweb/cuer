package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract

class PlaylistsDialogModelMapper() {

    fun map(
        playlistsModel: PlaylistsMviContract.View.Model?,
        config: PlaylistsMviDialogContract.Config,
        pinOn: Boolean
    ) = PlaylistsMviDialogContract.Model(
        playlistsModel,
        config.showAdd,
        config.showPin && pinOn,
        config.showPin && !pinOn
    )
}