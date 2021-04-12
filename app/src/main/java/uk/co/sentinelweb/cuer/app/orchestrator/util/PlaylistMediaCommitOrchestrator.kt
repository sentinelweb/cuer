package uk.co.sentinelweb.cuer.app.orchestrator.util

import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

/**
 * Checks for any existing media items in the playlist. then save the ones not existing
 */
class PlaylistMediaCommitOrchestrator constructor(
    private val mediaOrchestrator: MediaOrchestrator
) {
    suspend fun commitMediaAndReplace(playlist: PlaylistDomain, target: Source): PlaylistDomain {
        val existingMedia = mediaOrchestrator.loadList(
            PlatformIdListFilter(playlist.items.map { it.media.platformId }),
            Options(target, flat = false)
        )
        val existingMediaPlatformIds = existingMedia.map { it.platformId }
        val mediaLookup = playlist.items
            .map { it.media }
            .toMutableList()
            .apply { removeIf { existingMediaPlatformIds.contains(it.platformId) } }
            .map { it.copy(id = null) }
            .let { mediaOrchestrator.save(it, Options(target, flat = false)) }
            .toMutableList()
            .apply { addAll(existingMedia) }
            .associate { it.platformId to it }

        return playlist.copy(id = null,
            items = playlist.items.map {
                it.copy(
                    media = mediaLookup.get(it.media.platformId)
                        ?: throw java.lang.IllegalStateException("Media save failed")
                )
            })
    }
}