package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.RecentMediaFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.Recent
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain

class RecentItemsPlayistInteractor constructor(
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
//    private val mediaOrchestrator: MediaOrchestrator,
    private val log: LogWrapper
) : AppPlaylistInteractor {

    init {
        log.tag(this)
    }

    override val hasCustomDeleteAction = false
    override val customResources = null

    override suspend fun getPlaylist(): PlaylistDomain? =
        try {
            playlistItemOrchestrator
                .loadList(RecentMediaFilter(300), LOCAL.deepOptions())
                .let {
                    makeHeader()
                        .copy(items = it.mapIndexed { _, playlistItem -> playlistItem.copy() })
                }
        } catch (e: Exception) {
            log.e("Couldn't load recent items playlist", e)
            null
        }

    override fun makeHeader(): PlaylistDomain = PlaylistDomain(
        id = Recent.id.toIdentifier(MEMORY),
        title = "Recent",
        type = APP,
        currentIndex = -1,
        starred = false,
        image = ImageDomain(url = "https://cuer-275020.web.app/images/headers/pexels-ketut-subiyanto-4474038-600.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(
            playable = true,
            editable = false,
            deletable = false
        )
    )


    override fun makeStats(): PlaylistStatDomain = PlaylistStatDomain(
        playlistId = Recent.id.toIdentifier(MEMORY),
        itemCount = -1, // todo log in a background process and save to pref
        watchedItemCount = -1 // todo log in a background process and save to pref
    )

    override suspend fun performCustomDeleteAction(item: PlaylistItemDomain) = Unit
}