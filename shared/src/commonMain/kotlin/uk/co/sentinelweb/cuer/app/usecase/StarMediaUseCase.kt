package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class StarMediaUseCase(
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val log: LogWrapper,
) {
    init {
        log.tag(this)
    }

    suspend fun starMedia(itemId: OrchestratorContract.Identifier<GUID>): PlaylistItemDomain? {
        return itemId
            .takeIf { listOf(MEMORY, LOCAL).contains(itemId.source) }
            ?.let { playlistItemOrchestrator.loadById(itemId.id, itemId.deepOptions()) }
            ?.let { it.copy(media = it.media.copy(starred = !it.media.starred)) }
            ?.also { playlistItemOrchestrator.save(it, itemId.deepOptions()) }
    }
}
