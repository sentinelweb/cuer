package uk.co.sentinelweb.cuer.hub.ui.player.vlc

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.domain.PlaylistAndItemDomain

class FolderMemoryPlaylistItemLoader : PlayerContract.PlaylistItemLoader {

    private var playlistAndItemDomain: PlaylistAndItemDomain? = null
    override fun load(): PlaylistAndItemDomain? = playlistAndItemDomain

    fun setPlaylistAndItem(playlistAndItemDomain: PlaylistAndItemDomain) {
        this.playlistAndItemDomain = playlistAndItemDomain
    }
}