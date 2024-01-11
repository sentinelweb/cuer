package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

interface PlaylistMediaUpdateUsecaseContract {
    suspend fun updateMedia(
        playlist: PlaylistDomain,
        update: UpdateDomain<MediaDomain>,
        options: OrchestratorContract.Options
    ): MediaDomain
}