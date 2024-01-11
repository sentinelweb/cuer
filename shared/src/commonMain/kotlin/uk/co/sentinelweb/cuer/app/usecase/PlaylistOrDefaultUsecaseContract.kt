package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

interface PlaylistOrDefaultUsecaseContract {
    suspend fun getPlaylistOrDefault(playlistId: OrchestratorContract.Identifier<GUID>?): PlaylistDomain?
    suspend fun updateCurrentIndex(input: PlaylistDomain, options: OrchestratorContract.Options): Boolean
}