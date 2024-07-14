package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.net.client.ServiceExecutor
import uk.co.sentinelweb.cuer.net.ext.replaceUrlPlaceholder
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYER_COMMAND_API
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYER_COMMAND_API.P_ARG0
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYER_COMMAND_API.P_COMMAND
import uk.co.sentinelweb.cuer.remote.server.ipport
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerMessage
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerMessage.*

internal class RemotePlayerService(
    private val executor: ServiceExecutor
) {
    internal suspend fun executeCommand(locator: Locator, message: PlayerMessage) {
        val command = when (message) {
            is PlayPause -> PLAYER_COMMAND_API.PATH
                .replaceUrlPlaceholder(P_COMMAND, "PlayPause")
                .replaceUrlPlaceholder(P_ARG0, message.isPlaying.toString())

            is SeekToFraction -> PLAYER_COMMAND_API.PATH
                .replaceUrlPlaceholder(P_COMMAND, "SeekToFraction")
                .replaceUrlPlaceholder(P_ARG0, message.fraction.toString())

            is TrackSelected -> PLAYER_COMMAND_API.PATH
                .replaceUrlPlaceholder(P_COMMAND, "TrackSelected")
                .replaceUrlPlaceholder(P_ARG0, message.itemId.id.toString())
            // fixme not implemented on server: need to add source/resetPosition
            SkipBack -> PLAYER_COMMAND_API.PATH
                .replaceUrlPlaceholder(P_COMMAND, "SkipBack")

            SkipFwd -> PLAYER_COMMAND_API.PATH
                .replaceUrlPlaceholder(P_COMMAND, "SkipFwd")

            TrackBack -> PLAYER_COMMAND_API.PATH
                .replaceUrlPlaceholder(P_COMMAND, "TrackBack")

            TrackFwd -> PLAYER_COMMAND_API.PATH
                .replaceUrlPlaceholder(P_COMMAND, "TrackFwd")
        }
        executor.getResponse(path = locator.ipport() + command)
    }
}