package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class PlaylistMediaUpdateUsecase(
    private val mediaOrchestrator: MediaOrchestrator
) {
    suspend fun updateMedia(
        playlist: PlaylistDomain,
        update: UpdateDomain<MediaDomain>,
        options: OrchestratorContract.Options
    ): MediaDomain =
        when (options.source) {
            OrchestratorContract.Source.LOCAL -> mediaOrchestrator.update(update, options)
            else -> throw UnsupportedOperationException("Media update not supported for ${options.source}")
        }
}