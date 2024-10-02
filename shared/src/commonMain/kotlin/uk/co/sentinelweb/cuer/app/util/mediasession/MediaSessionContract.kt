package uk.co.sentinelweb.cuer.app.util.mediasession

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

interface MediaSessionContract {

    interface Manager {
        fun checkCreateMediaSession(controls: PlayerContract.PlayerControls.Listener)
        fun destroyMediaSession()
        fun setMedia(media: MediaDomain, playlist: PlaylistDomain?)
        fun updatePlaybackState(media: MediaDomain, state: PlayerStateDomain, liveOffset: Long?, playlist: PlaylistDomain?)
    }
}
