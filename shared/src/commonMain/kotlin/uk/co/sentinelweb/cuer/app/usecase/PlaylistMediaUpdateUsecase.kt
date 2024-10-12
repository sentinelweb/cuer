package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.*
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class PlaylistMediaUpdateUsecase(
    private val mediaOrchestrator: MediaOrchestrator,
    private val log:LogWrapper
) {
    init {
        log.tag(this)
    }

    suspend fun updateMedia(
        playlist: PlaylistDomain,
        update: UpdateDomain<MediaDomain>,
        options: OrchestratorContract.Options
    ): MediaDomain =
        when (options.source) {
            LOCAL, MEMORY -> mediaOrchestrator.update(update, options)
            //LOCAL_NETWORK -> // todo update remote
            else -> throw UnsupportedOperationException("Media update not supported for ${options.source}")
        }
}
