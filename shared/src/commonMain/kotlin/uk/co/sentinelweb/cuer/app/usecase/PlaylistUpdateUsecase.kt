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
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
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
        val reason: String = "",
        val numberItems: Int = -1,
        val newItems: List<PlaylistItemDomain>? = null
    )

    fun checkToUpdate(p: PlaylistDomain): Boolean = updateChecker.shouldUpdate(p)

    suspend fun update(playlistDomain: PlaylistDomain): UpdateResult {
        return playlistDomain
            .takeIf { playlistDomain.type == PLATFORM }
            ?.let {
                if (it.platform == null) {
                    playlistOrchestrator.save(it.copy(platform = YOUTUBE), LOCAL.flatOptions())
                } else it
            }
            ?.let { playlist ->
                when (playlist.platform) {
                    YOUTUBE -> playlist.platformId
                        ?.run {
                            playlistOrchestrator
                                .loadByPlatformId(this, Source.PLATFORM.deepOptions())
                                ?.let { removeExistingItems(it, playlist) }
                                ?.takeIf { it.items.size > 0 }
                                ?.let { playlistMediaLookupUsecase.lookupMediaAndReplace(it) }
                                ?.let { playlistItemOrchestrator.save(it.items, LOCAL.deepOptions()) }
                                ?.let { UpdateResult(true, numberItems = it.size, newItems = it) }
                                ?: UpdateResult(true, numberItems = 0)
                        }
                        ?: UpdateResult(false, reason = "No platform id")

                    else -> UpdateResult(false, reason = "Unsupported platform type")
                }
            }
            ?: UpdateResult(false, reason = "Not of PLATFORM type")
    }

    private suspend fun removeExistingItems(platform: PlaylistDomain, existing: PlaylistDomain): PlaylistDomain {

        val platformPlaylistExistingMediaPlatformIds =
            mediaOrchestrator.loadList(
                PlatformIdListFilter(platform.items.map { it.media.platformId }),
                LOCAL.flatOptions()
            ).map { it.platformId }
        //log.d("platformPlaylistExistingMediaPlatformIds: ${platformPlaylistExistingMediaPlatformIds.size}")

        val minOrder = if (existing.items.size > 0) existing.items.minOf { it.order } else 0
        val maxOrder = if (existing.items.size > 0) existing.items.maxOf { it.order } else 0
        val orderIsAscending = existing.orderIsAscending()

        log.d("minOrder: $minOrder maxOrder: $maxOrder orderIsAscending: $orderIsAscending")
        val newItems = platform.items.toMutableList()
            .apply { removeAll { platformPlaylistExistingMediaPlatformIds.contains(it.media.platformId) } }

        val playlistWithNewItemsOnly = existing.copy(items = newItems.mapIndexed { i, item ->
            item.copy(
                id = null,
                playlistId = existing.id,
                dateAdded = timeProvider.instant(),
                order = if (orderIsAscending) (maxOrder + (i + 1) * 1000) else (minOrder - ((newItems.size - i)) * 1000)
            )
        })
        //log.d(playlistWithNewItemsOnly.summarise())
        log.d("playlistWithNewItemsOnly: ${playlistWithNewItemsOnly.items.size}")
        return playlistWithNewItemsOnly
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