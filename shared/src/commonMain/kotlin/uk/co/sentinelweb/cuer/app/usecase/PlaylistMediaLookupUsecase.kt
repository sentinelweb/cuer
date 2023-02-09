package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.MediaIdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.PlatformIdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.ext.summarise

/**
 * Checks for any existing media items in the playlist.
 */
class PlaylistMediaLookupUsecase constructor(
    private val mediaOrchestrator: MediaOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val log: LogWrapper
) {
    init {
        log.tag(this)
    }

    suspend fun lookupMediaAndReplace(playlist: PlaylistDomain): PlaylistDomain {
        log.i("lookupMediaAndReplace: playlist: ${playlist.summarise()}")
        val mediaLookup = buildMediaLookup(playlist)
        //log.i("lookupMediaAndReplace: mediaLookup: $mediaLookup")
        return playlist.copy(
            id = null,
            items = playlist.items.map {
                it.copy(
                    media = mediaLookup.get(it.media.platformId)
                        ?: throw IllegalStateException("Media save failed")
                )
            })
    }

    suspend fun lookupPlaylistItemsAndReplace(playlist: PlaylistDomain): PlaylistDomain =
        buildMediaLookup(playlist)
            .let { it.values.mapNotNull { it.id?.id } }
            .let { playlistItemOrchestrator.loadList(MediaIdListFilter(it), LOCAL.deepOptions(emit = false)) }
            .distinctBy { it.media.platformId }
            .associateBy { it.media.platformId }
            .let { map ->
                playlist.copy(
                    items = playlist.items.map { item ->
                        map[item.media.platformId] ?: item
                    })
            }

    private suspend fun buildMediaLookup(
        playlist: PlaylistDomain
    ): Map<String, MediaDomain> =
        mediaOrchestrator.loadList(
            PlatformIdListFilter(playlist.items.map { it.media.platformId }),
            LOCAL.deepOptions(emit = false)
        ).let { existingMedia ->
            // fixme this crashes where the playlist has duplicate media in the source
            val existingMediaPlatformIds = existingMedia.map { it.platformId }
            //log.d("buildMediaLookup: existingMediaPlatformIds: ${existingMedia.map { "${it.id} ${it.platformId}" }}")
            playlist.items
                .map { it.media }
                .toMutableList()
                .apply { removeAll { existingMediaPlatformIds.contains(it.platformId) } }
                .map { it.copy(id = null) }
                .associateBy { it.platformId to it }
                .values.toList()
                .let { mediaOrchestrator.save(it, LOCAL.deepOptions(emit = false)) }
                .toMutableList()
                .apply { addAll(existingMedia) }
                .associate { it.platformId to it }
        }
}