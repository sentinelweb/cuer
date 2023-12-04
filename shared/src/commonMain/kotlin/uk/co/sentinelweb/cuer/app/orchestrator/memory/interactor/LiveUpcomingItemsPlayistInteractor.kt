package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.LiveUpcomingMediaFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.LiveUpcoming
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain

class LiveUpcomingItemsPlayistInteractor constructor(
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val mediaOrchestrator: MediaOrchestrator,
    private val log: LogWrapper,
) : AppPlaylistInteractor {

    init {
        log.tag(this)
    }

    override val hasCustomDeleteAction = true
    override val customResources = null

    override suspend fun getPlaylist(): PlaylistDomain? =
        try {
            playlistItemOrchestrator
                .loadList(LiveUpcomingMediaFilter(300), LOCAL.deepOptions())
                .let {
                    makeHeader()
                        .copy(items = it.mapIndexed { _, playlistItem -> playlistItem.copy() })
                }
        } catch (e: Exception) {
            log.e("Couldn't load nee items playlist", e)
            null
        }

    override fun makeHeader(): PlaylistDomain = PlaylistDomain(
        id = LiveUpcoming.id.toIdentifier(MEMORY),
        title = "Live / Upcoming",
        type = APP,
        currentIndex = -1,
        starred = true,
        image = ImageDomain(url = "https://cuer-275020.web.app/images/headers/jesus-loves-austin-0O0gux4OZX4-unsplash.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(
            playable = true,
            editable = false,
            deletable = false
        )
    )

    override fun makeStats(): PlaylistStatDomain = PlaylistStatDomain(
        playlistId = LiveUpcoming.id.toIdentifier(MEMORY),
        itemCount = -1, // todo log in a background process and save to pref
        watchedItemCount = -1 // todo log in a background process and save to pref
    )

    override suspend fun performCustomDeleteAction(item: PlaylistItemDomain) = Unit
}