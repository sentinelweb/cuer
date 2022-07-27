package uk.co.sentinelweb.cuer.app.orchestrator.util

import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM

class PlaylistUpdateOrchestrator constructor(
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val mediaOrchestrator: MediaOrchestrator,
    private val playlistMediaLookupOrchestrator: PlaylistMediaLookupOrchestrator,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper,
    private val updateChecker: UpdateCheck
) {

    init {
        log.tag(this)
    }

    fun checkToUpdate(p: PlaylistDomain): Boolean = updateChecker.shouldUpdate(p)

    suspend fun update(playlistDomain: PlaylistDomain) {
        playlistDomain.takeIf { playlistDomain.type == PLATFORM }
            ?.let {
                if (it.platform == null)
                    playlistOrchestrator.save(it.copy(platform = YOUTUBE), LOCAL.flatOptions())
                else it
            }
            ?.let { p ->
                when (p.platform) {
                    YOUTUBE -> p.platformId?.apply {
                        playlistOrchestrator
                            .load(this, Options(Source.PLATFORM, flat = false))
                            ?.let { removeExistingItems(it, p) }
                            ?.takeIf { it.items.size > 0 }
                            ?.let { playlistMediaLookupOrchestrator.lookupMediaAndReplace(it, LOCAL) }
                            ?.let { playlistItemOrchestrator.save(it.items, Options(LOCAL, flat = false)) }
                    }
                    else -> Unit
                }
            }
    }

    private suspend fun removeExistingItems(platform: PlaylistDomain, existing: PlaylistDomain): PlaylistDomain {
//        val existingPlaylistMediaPlatformIds = existing.items.map { it.media.platformId }
        val platformPlaylistExistingMediaPlatformIds =
            mediaOrchestrator.loadList(PlatformIdListFilter(platform.items.map { it.media.platformId }), LOCAL.flatOptions())
                .map { it.platformId }
        val maxOrder = if (existing.items.size>0) existing.items.maxOf { it.order } else 0
        val newItems =
            platform.items.toMutableList().apply { removeAll { platformPlaylistExistingMediaPlatformIds.contains(it.media.platformId) } }
        return existing.copy(items = newItems.mapIndexed { i, item ->
            item.copy(
                id = null,
                playlistId = existing.id,
                dateAdded = timeProvider.instant(),
                order = maxOrder + i * 1000
            )
        })
    }


    interface UpdateCheck {
        fun shouldUpdate(p: PlaylistDomain): Boolean
    }

    class PlatformUpdateCheck : UpdateCheck {
        override fun shouldUpdate(p: PlaylistDomain): Boolean =
            p.type == PLATFORM
    }

    companion object {
        private val UPDATE_INTERVAL_DEFAULT = 1000 * 60 * 60
    }
}