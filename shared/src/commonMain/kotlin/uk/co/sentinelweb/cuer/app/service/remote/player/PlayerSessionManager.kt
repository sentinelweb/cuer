package uk.co.sentinelweb.cuer.app.service.remote.player

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSession
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionHolder

class PlayerSessionManager(
    private val guidCreator: GuidCreator,
    private val playerSessionHolder: PlayerSessionHolder
) : PlayerSessionContract.Manager {

    override fun checkCreateMediaSession(controls: PlayerSessionContract.Listener) {
        if (playerSessionHolder.playerSession == null) {
            playerSessionHolder.playerSession = PlayerSession(
                guidCreator.create().toIdentifier(LOCAL),
                controls
            )
        } else {
            playerSessionHolder.playerSession?.controlsListener = controls
        }
    }

    override fun destroyMediaSession() {
        playerSessionHolder.playerSession = null
    }

    override fun setMedia(media: MediaDomain, playlist: PlaylistDomain?) {
        playerSessionHolder.playerSession?.media = media
        playerSessionHolder.playerSession?.playlist = playlist
    }

    override fun updatePlaybackState(
        media: MediaDomain,
        state: PlayerStateDomain,
        liveOffset: Long?,
        playlist: PlaylistDomain?
    ) {
        playerSessionHolder.playerSession?.media = media
        playerSessionHolder.playerSession?.playlist = playlist
        playerSessionHolder.playerSession?.liveOffset = liveOffset
        playerSessionHolder.playerSession?.playbackState = state
    }
}
