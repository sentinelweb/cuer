package uk.co.sentinelweb.cuer.app.util.mediasession

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class CompositeMediaSessionManager(
    private val sessions: List<MediaSessionContract.Manager>
) : MediaSessionContract.Manager {
    override fun checkCreateMediaSession(controls: PlayerContract.PlayerControls.Listener) {
        sessions.forEach { it.checkCreateMediaSession(controls) }
    }

    override fun destroyMediaSession() {
        sessions.forEach { it.destroyMediaSession() }
    }

    override fun setMedia(media: MediaDomain, playlist: PlaylistDomain?) {
        sessions.forEach { it.setMedia(media, playlist) }
    }

    override fun updatePlaybackState(
        media: MediaDomain,
        state: PlayerStateDomain,
        liveOffset: Long?,
        playlist: PlaylistDomain?
    ) {
        sessions.forEach {
            it.updatePlaybackState(
                media,
                state,
                liveOffset,
                playlist
            )
        }
    }

}