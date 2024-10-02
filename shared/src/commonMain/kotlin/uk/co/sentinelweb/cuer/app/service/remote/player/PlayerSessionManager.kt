package uk.co.sentinelweb.cuer.app.service.remote.player

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSession
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionHolder

class PlayerSessionManager(
    private val guidCreator: GuidCreator,
    private val playerSessionHolder: PlayerSessionHolder,
    private val log: LogWrapper,
) : PlayerSessionContract.Manager {

    init {
        log.tag(this)
    }

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

    override fun setVolume(volume: Float) {
        //log.d("session volume: $volume")
        playerSessionHolder.playerSession?.volume = volume
    }

    override fun setVolumeMax(volumeMax: Float) {
        //log.d("session volumeMax: $volumeMax")
        playerSessionHolder.playerSession?.volumeMax = volumeMax
    }

    override fun setScreen(screen: PlayerNodeDomain.Screen) {
        playerSessionHolder.playerSession?.screen = screen
    }
}
