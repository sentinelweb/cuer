import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL_NETWORK
import uk.co.sentinelweb.cuer.core.wrapper.URLEncoder
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.rewriteIdsToSource
import uk.co.sentinelweb.cuer.domain.ext.summarise
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.VIDEO_STREAM_API
import uk.co.sentinelweb.cuer.remote.server.http
import uk.co.sentinelweb.cuer.remote.server.locator

fun PlaylistItemDomain.summarise(): String =
    "ITEM: id: $id, order: $order, playlistId: $playlistId, media: ${media.summarise()}"


fun PlaylistItemDomain.rewriteIdsToSource(source: Source, locator: Locator?) = this.copy(
    id = this.id?.copy(source = source, locator = locator),
    media = this.media.rewriteIdsToSource(source, locator)
)

fun PlaylistItemDomain.httpLocalNetworkUrl(localRepository: LocalRepository) =
    this.takeIf { it.id != null && it.id.source == LOCAL_NETWORK && it.id.locator != null }
    ?.takeIf { localRepository.localNode.locator() != it.id?.locator }
    ?.takeIf { it.media.platform == PlatformDomain.FILESYSTEM }
    ?.let {
        it.copy(
            media = it.media.copy(
                platformId =
                "${it.id?.locator?.http()}${VIDEO_STREAM_API.ROUTE}/${
                    URLEncoder.encode(it.media.platformId, "UTF-8")
                }"
            )
        )
    }
    ?.media
    ?.platformId
