package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.StarredMediaFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.Starred
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain

class StarredItemsPlayistInteractor constructor(
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
                .loadList(StarredMediaFilter(300), LOCAL.deepOptions())
                .let {
                    makeHeader()
                        .copy(items = it.mapIndexed { _, playlistItem -> playlistItem.copy() })
                }
        } catch (e: Exception) {
            log.e("Couldn't load starred items playlist", e)
            null
        }

    override fun makeHeader(): PlaylistDomain = PlaylistDomain(
        id = Starred.id,
        title = "Starred items",
        type = APP,
        currentIndex = -1,
        starred = true,
        image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/pixabay-star-640-wallpaper-ga4c7c7acf_640.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(
            playable = false,
            editable = false,
            deletable = false
        )
    )


    override fun makeStats(): PlaylistStatDomain = PlaylistStatDomain(
        playlistId = Starred.id,
        itemCount = -1, // todo log in a background process and save to pref
        watchedItemCount = -1 // todo log in a background process and save to pref
    )

    override suspend fun performCustomDeleteAction(item: PlaylistItemDomain) {
        mediaOrchestrator.save(item.media.copy(starred = false), LOCAL.flatOptions())
    }
}