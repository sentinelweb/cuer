package uk.co.sentinelweb.cuer.app.util.mediasession

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface MediaSessionContract {

    interface Manager {
        fun checkCreateMediaSession(controls: PlayerContract.PlayerControls.Listener)
        fun destroyMediaSession()
        fun setItem(item: PlaylistItemDomain, playlist: PlaylistDomain?)
        fun updatePlaybackState(item: PlaylistItemDomain, state: PlayerStateDomain, liveOffset: Long?, playlist: PlaylistDomain?)
    }
}
