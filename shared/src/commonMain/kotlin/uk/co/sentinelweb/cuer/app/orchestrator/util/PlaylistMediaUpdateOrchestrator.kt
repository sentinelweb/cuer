package uk.co.sentinelweb.cuer.app.orchestrator.util

import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class PlaylistMediaUpdateOrchestrator constructor(
    private val mediaOrchestrator: MediaOrchestrator
) {
    suspend fun updateMedia(
        playlist: PlaylistDomain,
        update: UpdateDomain<MediaDomain>,
        options: OrchestratorContract.Options
    ): MediaDomain? =
        when (options.source) {
            OrchestratorContract.Source.MEMORY -> if (playlist.type == PlaylistDomain.PlaylistTypeDomain.APP) {
                mediaOrchestrator.update(update, options.copy(source = OrchestratorContract.Source.LOCAL))
            } else null
            OrchestratorContract.Source.LOCAL -> mediaOrchestrator.update(update, options)
            else -> throw UnsupportedOperationException("Media update not suppoted for ${options.source}")
        }
}