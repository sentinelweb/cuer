package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.SearchFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.LocalSearch
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP

class LocalSearchPlayistInteractor constructor(
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val prefsWrapper: MultiPlatformPreferencesWrapper,
    private val log: LogWrapper
) : AppPlaylistInteractor {

    init {
        log.tag(this)
    }

    override val hasCustomDeleteAction = false
    override val customResources = null

    fun search(): SearchLocalDomain? = prefsWrapper.lastLocalSearch

    override suspend fun getPlaylist(): PlaylistDomain? =
        try {
            search()
                ?.let { mapToFilter(it) }
                ?.let {
                    playlistItemOrchestrator
                        .loadList(it, LOCAL.deepOptions())
                        .let {
                            makeHeader()
                                .copy(items = it.mapIndexed { _, playlistItem -> playlistItem.copy() })
                        }
                }
        } catch (e: Exception) {
            log.e("Couldn't load local search playlist", e)
            null
        }

    private fun mapToFilter(searchDomain: SearchLocalDomain) = SearchFilter(
        text = searchDomain.text,
        isWatched = searchDomain.isWatched,
        isNew = searchDomain.isNew,
        isLive = searchDomain.isLive,
        playlistIds = if (searchDomain.playlists.isEmpty()) null
        else searchDomain.playlists.mapNotNull { it.id?.id }
    )

    override fun makeHeader(): PlaylistDomain = PlaylistDomain(
        id = LocalSearch.id.toIdentifier(MEMORY),
        title = mapTitle(),
        type = APP,
        currentIndex = -1,
        starred = true,
        image = ImageDomain(url = "https://cuer-275020.web.app/images/headers/pexels-noelle-otto-906055.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(
            playable = false,
            editable = false
        )
    )

    private fun mapTitle() =
        "Local Search: " +
                search()?.let {
                    it.text + it.playlists.let {
                        if (it.isNotEmpty()) " " + it.map { it.title } else ""
                    }
                }


    override fun makeStats(): PlaylistStatDomain = PlaylistStatDomain(
        playlistId = LocalSearch.id.toIdentifier(MEMORY),
        itemCount = -1,
        watchedItemCount = -1
    )

    override suspend fun performCustomDeleteAction(item: PlaylistItemDomain) = Unit
}