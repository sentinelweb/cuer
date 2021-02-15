package uk.co.sentinelweb.cuer.app.orchestrator

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.domain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM

class PlaylistUpdateOrchestrator constructor(
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val mediaOrchestrator: MediaOrchestrator,
    private val playlistMediaCommitOrchestrator: PlaylistMediaCommitOrchestrator,
    private val timeProvider: TimeProvider,
    private val updateChecker: UpdateCheck = PlatformUpdateCheck()
) {

    fun checkToUpdate(p: PlaylistDomain): Boolean = updateChecker.shouldUpdate(p)

    suspend fun update(p: PlaylistDomain) {
        if (p.type == PLATFORM) {
            when (p.platform) {
                YOUTUBE -> p.platformId?.apply {
                    playlistOrchestrator
                        .load(this, Options(Source.PLATFORM, flat = false))
                        ?.let { removeExistingItems(it, p) }
                        ?.takeIf { it.items.size > 0 }
                        ?.let { playlistMediaCommitOrchestrator.commitMediaAndReplace(it, LOCAL) }
                        ?.let { playlistItemOrchestrator.save(it.items, Options(LOCAL, flat = false)) }
                }
                else -> Unit
            }
        }
    }

    private suspend fun removeExistingItems(platform: PlaylistDomain, existing: PlaylistDomain): PlaylistDomain {
        val existingPlaylistMediaPlatformIds = existing.items.map { it.media.platformId }
        val platformPlaylistExistingMediaPlatformIds =
            mediaOrchestrator.loadList(PlatformIdListFilter(platform.items.map { it.media.platformId }), Options(LOCAL))
                .map { it.platformId }
        val maxOrder = existing.items.maxOf { it.order }
        val newItems =
            platform.items.toMutableList().apply { removeIf { platformPlaylistExistingMediaPlatformIds.contains(it.media.platformId) } }
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