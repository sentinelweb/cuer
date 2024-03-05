package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.PlatformIdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.LiveUpcoming
import uk.co.sentinelweb.cuer.app.service.update.UpdateServiceContract
import uk.co.sentinelweb.cuer.app.usecase.PlaylistUpdateUsecase.UpdateResult.Result
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.orderIsAscending
import uk.co.sentinelweb.cuer.domain.ext.summarise

class PlaylistUpdateUsecase constructor(
    private val playlistOrchestrator: OrchestratorContract<PlaylistDomain>,
    private val playlistItemOrchestrator: OrchestratorContract<PlaylistItemDomain>,
    private val mediaOrchestrator: OrchestratorContract<MediaDomain>,
    private val playlistMediaLookupUsecase: PlaylistMediaLookupUsecase,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper,
    private val updateChecker: UpdateCheck,
    private val updateServiceManager: UpdateServiceContract.Manager
) {

    init {
        log.tag(this)
    }


    data class UpdateResult(
        val status: Result,
        val reason: String = "",
        val numberItems: Int = -1,
        val newItems: List<PlaylistItemDomain>? = null
    ) {
        enum class Result{Success, Failure, Pending}
    }

    fun checkToUpdate(p: PlaylistDomain): Boolean = updateChecker.checkToUpdate(p)
    fun canUpdate(p: PlaylistDomain): Boolean = updateChecker.canUpdate(p)

    suspend fun update(id: OrchestratorContract.Identifier<GUID>): UpdateResult {
        return if (id == LiveUpcoming.identifier()) {
            log.d("Update service liveupcoming")
            updateServiceManager.start()
            UpdateResult(Result.Pending, "Updating liveUpcoming ..")
        } else {
            playlistOrchestrator.loadById(id.id, id.source.deepOptions())
                ?.let { update(it) }
                ?: UpdateResult(Result.Failure, "Playlist not found")
        }
    }

    suspend fun update(playlistDomain: PlaylistDomain): UpdateResult {
        return if (playlistDomain.id == LiveUpcoming.identifier()) {
            // divert LiveUpcoming playlist to service
            // todo actually all updates should goto the service .. eventually
            log.d("Update service liveupcoming")
            updateServiceManager.start()
            UpdateResult(Result.Pending, "Updating liveUpcoming ..")
        } else {
            playlistDomain
                .takeIf { canUpdate(it) }
                ?.let {
                    if (it.platform == null) {
                        playlistOrchestrator.save(it.copy(platform = YOUTUBE), LOCAL.flatOptions())
                    } else it
                }
                ?.let { playlist ->
                    when (playlist.platform ?: false) {
                        YOUTUBE -> playlist.platformId
                            ?.let { id ->
                                playlistOrchestrator
                                    .loadByPlatformId(id, Source.PLATFORM.deepOptions())
                                    ?.let { removeExistingItems(it, playlist) }
                                    ?.takeIf { it.items.size > 0 }
                                    ?.let { playlistMediaLookupUsecase.lookupMediaAndReplace(it) }
                                    ?.let { playlistItemOrchestrator.save(it.items, LOCAL.deepOptions()) }
                                    ?.let { UpdateResult(Result.Success, numberItems = it.size, newItems = it) }
                                    ?: UpdateResult(Result.Success, numberItems = 0)
                            }
                            ?: UpdateResult(Result.Failure, reason = "No platformId")
                        else -> UpdateResult(Result.Failure, reason = "Unsupported platform type")
                    }
                }
                ?: UpdateResult(Result.Failure, reason = "Cannot update this playlist")
        }
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
                order = if (orderIsAscending) (maxOrder + (i + 1) * SECONDS) else (minOrder - ((newItems.size - i)) * SECONDS)
            )
        })
        log.d(playlistWithNewItemsOnly.summarise())
        //log.d("playlistWithNewItemsOnly: ${playlistWithNewItemsOnly.items.size}")
        return playlistWithNewItemsOnly
    }

    interface UpdateCheck {
        fun checkToUpdate(p: PlaylistDomain): Boolean
        fun canUpdate(p: PlaylistDomain): Boolean
    }

    class PlatformUpdateCheck : UpdateCheck {
        override fun checkToUpdate(p: PlaylistDomain): Boolean =
            p.type == PLATFORM || p.id == LiveUpcoming.identifier()

        override fun canUpdate(p: PlaylistDomain): Boolean =
            (p.platformId != null && p.platform == YOUTUBE && p.type == PLATFORM)
                    || p.id == LiveUpcoming.identifier()
    }

    companion object {
        val SECONDS = 1000L
    }
}
