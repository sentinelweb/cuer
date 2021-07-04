package uk.co.sentinelweb.cuer.app.orchestrator.util

import uk.co.sentinelweb.cuer.app.db.repository.PlaylistItemDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

/**
 * Checks for any existing media items in the playlist.
 */
class PlaylistMediaLookupOrchestrator constructor(
    private val mediaOrchestrator: MediaOrchestrator,
    private val roomPlaylistItemDatabaseRepository: PlaylistItemDatabaseRepository
) {
    suspend fun lookupMediaAndReplace(playlist: PlaylistDomain, target: Source): PlaylistDomain {
        val mediaLookup = buildMediaLookup(playlist, target)

        return playlist.copy(id = null,
            items = playlist.items.map {
                it.copy(
                    media = mediaLookup.get(it.media.platformId)
                        ?: throw IllegalStateException("Media save failed")
                )
            })
    }

    suspend fun lookupPlaylistItemsAndReplace(playlist: PlaylistDomain): PlaylistDomain =
        buildMediaLookup(playlist, Source.LOCAL)
            .let { it.values.mapNotNull { it.id } }
            .let { roomPlaylistItemDatabaseRepository.loadList(MediaIdListFilter(it)) }
            .data
            ?.distinctBy { it.media.platformId }
            ?.associateBy { it.media.platformId }
            ?.let { map ->
                playlist.copy(
                    items = playlist.items.map { item ->
                        map[item.media.platformId] ?: item
                    })
            } ?: playlist


    // todo consider moving this to playlist save
    private suspend fun buildMediaLookup(
        playlist: PlaylistDomain, target: Source
    ): Map<String, MediaDomain> {
        val existingMedia = mediaOrchestrator.loadList(
            PlatformIdListFilter(playlist.items.map { it.media.platformId }),
            Options(target, flat = false)
        )
        val existingMediaPlatformIds = existingMedia.map { it.platformId }
        val mediaLookup = playlist.items
            .map { it.media }
            .toMutableList()
            .apply { removeAll { existingMediaPlatformIds.contains(it.platformId) } }
            .map { it.copy(id = null) }
            .let { mediaOrchestrator.save(it, Options(target, flat = false)) }
            .toMutableList()
            .apply { addAll(existingMedia) }
            .associate { it.platformId to it }
        return mediaLookup
    }
}