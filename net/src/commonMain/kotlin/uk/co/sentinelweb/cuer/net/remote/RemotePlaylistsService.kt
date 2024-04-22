package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.domain.system.ResponseDomain
import uk.co.sentinelweb.cuer.net.client.ServiceExecutor
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYLISTS_API
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYLIST_API
import uk.co.sentinelweb.cuer.remote.server.ipport

internal class RemotePlaylistsService(
    private val executor: ServiceExecutor
) {

    internal suspend fun getPlaylists(
        node: RemoteNodeDomain
    ): ResponseDomain = executor.get<ResponseDomain>(
        path = node.ipport() + PLAYLISTS_API.PATH
    )

    internal suspend fun getPlaylist(
        id: OrchestratorContract.Identifier<GUID>
    ): ResponseDomain = executor.get<ResponseDomain>(
        path = ((id.locator ?: throw IllegalArgumentException()).ipport() + PLAYLIST_API.PATH)
            .replace("{id}", id.id.value) // fixme use whatever ktor way
    )
}
