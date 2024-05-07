package uk.co.sentinelweb.cuer.app.service.remote.player

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerMessage

class PlayerMessageToIntentMapper(
    private val itemOrchestrator: PlaylistItemOrchestrator,
) {
    suspend fun map(message: PlayerMessage): Intent = when (message) {
        is PlayerMessage.PlayPause -> Intent.PlayPause(message.isPlaying)
        is PlayerMessage.SeekToFraction -> Intent.SeekToFraction(message.fraction)
        PlayerMessage.SkipBack -> Intent.SkipBack
        PlayerMessage.SkipFwd -> Intent.SkipFwd
        PlayerMessage.TrackBack -> Intent.TrackBack
        PlayerMessage.TrackFwd -> Intent.TrackFwd
        is PlayerMessage.TrackSelected -> {
            itemOrchestrator.loadById(message.itemId.id, LOCAL.deepOptions())
                ?.let { Intent.TrackSelected(it, message.resetPosition) }
                ?: throw IllegalArgumentException("item doesn't exist")
        }
    }
}