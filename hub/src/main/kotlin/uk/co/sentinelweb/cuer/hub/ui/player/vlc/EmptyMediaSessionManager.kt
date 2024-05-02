package uk.co.sentinelweb.cuer.hub.ui.player.vlc

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionContract
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class EmptyMediaSessionManager : MediaSessionContract.Manager {
    override fun checkCreateMediaSession(controls: PlayerContract.PlayerControls.Listener) = Unit

    override fun destroyMediaSession() = Unit

    override fun setMedia(media: MediaDomain, playlist: PlaylistDomain?) = Unit

    override fun updatePlaybackState(
        media: MediaDomain,
        state: PlayerStateDomain,
        liveOffset: Long?,
        playlist: PlaylistDomain?
    ) = Unit
}