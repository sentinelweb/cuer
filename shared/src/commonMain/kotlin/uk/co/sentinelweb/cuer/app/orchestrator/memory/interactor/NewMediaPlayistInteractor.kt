package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.NewMediaFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.NewItems
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.update.MediaPositionUpdateDomain

class NewMediaPlayistInteractor constructor(
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
                .loadList(NewMediaFilter(300), LOCAL.deepOptions())
                .let {
                    makeHeader()
                        .copy(items = it.mapIndexed { _, playlistItem -> playlistItem.copy() })
                }
        } catch (e: Exception) {
            log.e("Couldn't load new items playlist", e)
            null
        }

    override fun makeHeader(): PlaylistDomain = PlaylistDomain(
        id = NewItems.id,
        title = "New",
        type = APP,
        currentIndex = -1,
        starred = false,
        image = ImageDomain(url = "https://cuer-275020.web.app/images/headers/pexels-pixabay-40663-600.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(
            playable = false,
            editable = false,
            deletable = false,
            deletableItems = true
        )
    )

    override fun makeStats(): PlaylistStatDomain = PlaylistStatDomain(
        playlistId = NewItems.id,
        itemCount = -1, // todo log in a background process and save to pref
        watchedItemCount = 0 // todo log in a background process and save to pref
    )

    override suspend fun performCustomDeleteAction(item: PlaylistItemDomain) {
        mediaOrchestrator.update(
            MediaPositionUpdateDomain(
                id = item.media.id!!,
                duration = item.media.duration,
                positon = item.media.positon,
                dateLastPlayed = item.media.dateLastPlayed,
                watched = true,
            ), LOCAL.flatOptions()
        )
    }
}