package uk.co.sentinelweb.cuer.app.service.remote.player

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerCommandMessage

class PlayerMessageToIntentMapper(
    private val itemOrchestrator: PlaylistItemOrchestrator,
) {
    suspend fun map(message: PlayerCommandMessage): Intent = when (message) {
        is PlayerCommandMessage.PlayPause -> Intent.PlayPause(message.isPlaying)
        is PlayerCommandMessage.SeekToFraction -> Intent.SeekToFraction(message.fraction)
        PlayerCommandMessage.SkipBack -> Intent.SkipBack
        PlayerCommandMessage.SkipFwd -> Intent.SkipFwd
        PlayerCommandMessage.TrackBack -> Intent.TrackBack
        PlayerCommandMessage.TrackFwd -> Intent.TrackFwd
        is PlayerCommandMessage.TrackSelected -> {
            itemOrchestrator.loadById(message.itemId.id, LOCAL.deepOptions())
                ?.let { Intent.TrackSelected(it, message.resetPosition) }
                ?: throw IllegalArgumentException("item doesn't exist")
        }
    }
}