package uk.co.sentinelweb.cuer.app.orchestrator.util

import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.DefaultFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.MediaIdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.PLATFORM
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.Domain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.MEDIA
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class AddLinkOrchestrator constructor(
    private val mediaOrchestrator: MediaOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val linkScanner: LinkScanner,
    private val coProvider: CoroutineContextProvider,
) {
    suspend fun scanUrl(uriString: String): Domain = withContext(coProvider.IO) {
        linkScanner
            .scan(uriString)
            ?.let { scannedMedia ->
                when (scannedMedia.first) {
                    MEDIA -> (scannedMedia.second as MediaDomain)
                        .let {
                            mediaOrchestrator
                                .load(it.platformId, LOCAL.flatOptions())
                                ?.takeIf { it.id != null }
                                ?.let {
                                    playlistItemOrchestrator
                                        .loadList(MediaIdListFilter(listOf(it.id!!)), LOCAL.flatOptions())
                                        .takeIf { it.size > 0 }
                                        ?.get(0)
                                }
                                ?: mediaOrchestrator
                                    .load(it.platformId, PLATFORM.flatOptions())
                        }
//                  ObjectTypeDomain.PLAYLIST -> (scannedMedia.second as PlaylistDomain).apply {
//
//                  }
                    else -> throw UnsupportedOperationException("${scannedMedia.first} not supported yet")
                }
            }
    } ?: throw Exception("Couldn't scan link")

    suspend fun commitPlaylistItem(item: PlaylistItemDomain) =
        item.playlistId
            ?.let { playlistItemOrchestrator.save(item, LOCAL.deepOptions()) }
            ?: let {
                playlistOrchestrator.loadList(DefaultFilter, LOCAL.flatOptions())
                    .get(0)// throws exception if not found but should always be there
                    .let { item.copy(playlistId = it.id) }
                    .let { playlistItemOrchestrator.save(it, LOCAL.deepOptions()) }
            }


}
