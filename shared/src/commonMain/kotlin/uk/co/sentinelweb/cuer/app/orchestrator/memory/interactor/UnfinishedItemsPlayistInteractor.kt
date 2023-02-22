package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.UnfinishedMediaFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.Unfinished
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.update.MediaPositionUpdateDomain

class UnfinishedItemsPlayistInteractor constructor(
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val mediaOrchestrator: MediaOrchestrator,
    private val log: LogWrapper,
    override val customResources: AppPlaylistInteractor.CustomisationResources,
) : AppPlaylistInteractor {
    init {
        log.tag(this)
    }

    override val hasCustomDeleteAction = true

    override suspend fun getPlaylist(): PlaylistDomain? =
        try {
            playlistItemOrchestrator
                .loadList(UnfinishedMediaFilter(20, 60, 300), LOCAL.deepOptions())
                .let {
                    makeHeader()
                        .copy(items = it.mapIndexed { _, playlistItem -> playlistItem.copy() })
                }
        } catch (e: Exception) {
            log.e("Couldn't load nee items playlist", e)
            null
        }

    override fun makeHeader(): PlaylistDomain = PlaylistDomain(
        id = Unfinished.id.toIdentifier(MEMORY),
        title = "Unfinished",
        type = APP,
        currentIndex = -1,
        starred = true,
        image = ImageDomain(url = "https://cuer-275020.web.app/images/headers/florencia-viadana-crop-RqRCejwnccw-unsplash.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(
            playable = true,
            editable = false,
            deletable = false
        )
    )

    override fun makeStats(): PlaylistStatDomain = PlaylistStatDomain(
        playlistId = Unfinished.id.toIdentifier(MEMORY),
        itemCount = -1, // todo log in a background process and save to pref
        watchedItemCount = -1 // todo log in a background process and save to pref
    )

    override suspend fun performCustomDeleteAction(item: PlaylistItemDomain) {
        mediaOrchestrator.update(
            MediaPositionUpdateDomain(
                id = item.media.id!!,
                duration = item.media.duration,
                positon = item.media.duration,
                dateLastPlayed = item.media.dateLastPlayed,
                watched = false,
            ), LOCAL.flatOptions()
        )
    }
}