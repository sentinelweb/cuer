package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.system.ResponseDomain
import uk.co.sentinelweb.cuer.net.client.ServiceExecutor
import uk.co.sentinelweb.cuer.net.ext.replaceUrlPlaceholder
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYER_COMMAND_API
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYER_COMMAND_API.P_ARG0
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYER_COMMAND_API.P_COMMAND
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYER_LAUNCH_VIDEO_API
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYER_STATUS_API
import uk.co.sentinelweb.cuer.remote.server.ipport
import uk.co.sentinelweb.cuer.remote.server.message.ResponseMessage
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerCommandMessage
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerCommandMessage.*

internal class RemotePlayerService(
    private val executor: ServiceExecutor
) {
    internal suspend fun executeCommand(locator: Locator, message: PlayerCommandMessage) {
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
        executor.getResponseDomain(path = locator.ipport() + command)
    }

    internal suspend fun executeGetConfig(locator: Locator): ResponseDomain =
        executor.getResponseDomain(
            path = locator.ipport() + RemoteWebServerContract.Companion.PLAYER_CONFIG_API.PATH
        )

    internal suspend fun executeLaunchVideo(
        locator: Locator,
        item: PlaylistItemDomain,
        screenIndex: Int
    ): ResponseDomain {
        return executor.postResponse(
            path = locator.ipport() + PLAYER_LAUNCH_VIDEO_API.PATH,
            urlParams = mapOf(
                PLAYER_LAUNCH_VIDEO_API.P_SCREEN_INDEX to screenIndex.toString()
            ),
            postData = item
        )
    }

    internal suspend fun executeGetPlayerStatus(locator: Locator): ResponseMessage =
        executor.getResponseMessage(
            path = locator.ipport() + PLAYER_STATUS_API.PATH
        )
}