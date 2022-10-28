package uk.co.sentinelweb.cuer.app.orchestrator.util

import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.MediaIdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.PlatformIdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

/**
 * Checks for any existing media items in the playlist.
 */
class PlaylistMediaLookupOrchestrator constructor(
    private val mediaOrchestrator: MediaOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator
) {
    suspend fun lookupMediaAndReplace(playlist: PlaylistDomain): PlaylistDomain {
        val mediaLookup = buildMediaLookup(playlist)

        return playlist.copy(id = null,
            items = playlist.items.map {
                it.copy(
                    media = mediaLookup.get(it.media.platformId)
                        ?: throw IllegalStateException("Media save failed")
                )
            })
    }

    suspend fun lookupPlaylistItemsAndReplace(playlist: PlaylistDomain): PlaylistDomain =
        buildMediaLookup(playlist)
            .let { it.values.mapNotNull { it.id } }
            .let { playlistItemOrchestrator.loadList(MediaIdListFilter(it), LOCAL.deepOptions(emit = false)) }
            .distinctBy { it.media.platformId }
            .associateBy { it.media.platformId }
            .let { map ->
                playlist.copy(
                    items = playlist.items.map { item ->
                        map[item.media.platformId] ?: item
                    })
            } ?: playlist

    private suspend fun buildMediaLookup(
        playlist: PlaylistDomain
    ): Map<String, MediaDomain> =
        mediaOrchestrator.loadList(
            PlatformIdListFilter(playlist.items.map { it.media.platformId }),
            LOCAL.deepOptions(emit = false)
        ).let { existingMedia ->
            val existingMediaPlatformIds = existingMedia.map { it.platformId }
            playlist.items
                .map { it.media }
                .toMutableList()
                .apply { removeAll { existingMediaPlatformIds.contains(it.platformId) } }
                .map { it.copy(id = null) }
                .let { mediaOrchestrator.save(it, LOCAL.deepOptions(emit = false)) }
                .toMutableList()
                .apply { addAll(existingMedia) }
                .associate { it.platformId to it }
        }
}