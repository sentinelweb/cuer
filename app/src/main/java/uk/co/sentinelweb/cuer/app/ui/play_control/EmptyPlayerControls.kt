package uk.co.sentinelweb.cuer.app.ui.play_control

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class EmptyPlayerControls : PlayerContract.PlayerControls {
    override fun initMediaRouteButton() = Unit

    override fun setConnectionState(connState: PlayerContract.ConnectionState) = Unit

    override fun setPlayerState(playState: PlayerStateDomain) = Unit

    override fun addListener(l: PlayerContract.PlayerControls.Listener) = Unit

    override fun removeListener(l: PlayerContract.PlayerControls.Listener) = Unit

    override fun setCurrentSecond(second: Float) = Unit

    override fun setDuration(duration: Float) = Unit

    override fun error(msg: String) = Unit

    override fun setTitle(title: String) = Unit

    override fun reset() = Unit

    override fun restoreState() = Unit

    override fun setPlaylistName(name: String) = Unit

    override fun setPlaylistImage(image: ImageDomain?) = Unit

    override fun setPlaylistItem(
        playlistItem: PlaylistItemDomain?,
        source: OrchestratorContract.Source
    ) = Unit

    override fun disconnectSource() = Unit

    override fun seekTo(ms: Long) = Unit

    override fun getPlaylistItem(): PlaylistItemDomain? = null
}
