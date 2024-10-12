import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.rewriteIdsToSource
import uk.co.sentinelweb.cuer.domain.ext.summarise

fun PlaylistItemDomain.summarise(): String =
    "ITEM: id: $id, order: $order, playlistId: $playlistId, media: ${media.summarise()}"


fun PlaylistItemDomain.rewriteIdsToSource(source: Source, locator: Locator?) = this.copy(
    id = this.id?.copy(source = source, locator = locator),
    media = this.media.rewriteIdsToSource(source, locator)
)
