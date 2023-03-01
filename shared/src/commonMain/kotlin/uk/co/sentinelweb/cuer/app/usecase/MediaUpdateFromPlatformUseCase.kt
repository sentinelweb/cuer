package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.PLATFORM
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class MediaUpdateFromPlatformUseCase(
    private val connectivity: ConnectivityWrapper,
    private val mediaOrchestrator: MediaOrchestrator,
) {
    suspend operator fun invoke(originalMedia: MediaDomain): MediaDomain? =
        if (connectivity.isConnected()) {
            mediaOrchestrator.loadByPlatformId(originalMedia.platformId, PLATFORM.flatOptions())
                ?.copy(
                    id = originalMedia.id,
                    dateLastPlayed = originalMedia.dateLastPlayed,
                    starred = originalMedia.starred,
                    watched = originalMedia.watched,
                )
        } else null

    suspend fun checkToUpdateItem(item: PlaylistItemDomain): PlaylistItemDomain =
        if (item.media.duration == null) {
            invoke(item.media)
                ?.let { item.copy(media = it) }
                ?: item
        } else item

}