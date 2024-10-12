package uk.co.sentinelweb.cuer.domain.ext

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.REMOTE
import uk.co.sentinelweb.cuer.domain.PlaylistAndChildrenDomain

fun PlaylistAndChildrenDomain.rewriteIdsToSource(source: Source, locator: Locator?) = copy(
    playlist = this.playlist.rewriteIdsToSource(source, locator),
    children = this.children.map { childPlaylist ->
        childPlaylist.copy(
            id = childPlaylist.id?.copy(source = REMOTE, locator = locator)
        )
    }
)
