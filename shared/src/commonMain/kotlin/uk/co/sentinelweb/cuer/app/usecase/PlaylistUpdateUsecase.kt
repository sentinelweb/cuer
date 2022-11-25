package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.PlatformIdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM
import uk.co.sentinelweb.cuer.domain.ext.orderIsAscending

class PlaylistUpdateUsecase constructor(
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val mediaOrchestrator: MediaOrchestrator,
    private val playlistMediaLookupUsecase: PlaylistMediaLookupUsecase,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper,
    private val updateChecker: UpdateCheck
) {

    init {
        log.tag(this)
    }

    data class UpdateResult(
        val success: Boolean,
        val numberItems: Int
    )

    private val updateFail = UpdateResult(false, -1)

    fun checkToUpdate(p: PlaylistDomain): Boolean = updateChecker.shouldUpdate(p)

    suspend fun update(playlistDomain: PlaylistDomain): UpdateResult {
        return playlistDomain.takeIf { playlistDomain.type == PLATFORM }
            ?.let {
                if (it.platform == null) {
                    playlistOrchestrator.save(it.copy(platform = YOUTUBE), LOCAL.flatOptions())
                } else it
            }
            ?.let { p ->
                when (p.platform) {
                    YOUTUBE -> p.platformId?.run {
                        playlistOrchestrator
                            .loadByPlatformId(this, Source.PLATFORM.deepOptions())
                            ?.let { removeExistingItems(it, p) }
                            ?.takeIf { it.items.size > 0 }
                            ?.let { playlistMediaLookupUsecase.lookupMediaAndReplace(it) }
                            ?.let { playlistItemOrchestrator.save(it.items, LOCAL.deepOptions()) }
                            ?.let { UpdateResult(true, it.size) }
                    } ?: updateFail

                    else -> updateFail
                }
            }
            ?: updateFail
    }

    private suspend fun removeExistingItems(platform: PlaylistDomain, existing: PlaylistDomain): PlaylistDomain {
        val platformPlaylistExistingMediaPlatformIds =
            mediaOrchestrator.loadList(
                PlatformIdListFilter(platform.items.map { it.media.platformId }),
                LOCAL.flatOptions()
            )
                .map { it.platformId }
        val minOrder = if (existing.items.size > 0) existing.items.minOf { it.order } else 0
        val maxOrder = if (existing.items.size > 0) existing.items.maxOf { it.order } else 0
        val orderIsAscending = existing.orderIsAscending()
        val newItems =
            platform.items.toMutableList()
                .apply { removeAll { platformPlaylistExistingMediaPlatformIds.contains(it.media.platformId) } }
        return existing.copy(items = newItems.mapIndexed { i, item ->
            item.copy(
                id = null,
                playlistId = existing.id,
                dateAdded = timeProvider.instant(),
                order = if (orderIsAscending) (maxOrder + (i + 1) * 1000) else (minOrder - (i + 1) * 1000)
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