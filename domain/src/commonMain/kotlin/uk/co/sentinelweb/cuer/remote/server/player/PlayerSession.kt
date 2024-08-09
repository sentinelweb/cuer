package uk.co.sentinelweb.cuer.remote.server.player

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.*

// singleton class to hold the player session.
class PlayerSessionHolder {
    var playerSession: PlayerSession? = null
}

// the player session data - also hold controls back to the player MVI.
class PlayerSession(
    val id: OrchestratorContract.Identifier<GUID>,
    var controlsListener: PlayerSessionContract.Listener,
) {
    //var item: PlaylistItemDomain? = null
    var media: MediaDomain? = null
    var playlist: PlaylistDomain? = null
    var playbackState: PlayerStateDomain? = null
    var liveOffset: Long? = null
    var volume: Float = 0f
    var volumeMax: Float = 0f
    var screen: PlayerNodeDomain.Screen? = null
}
