package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.YoutubeSearch
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.usecase.PlaylistMediaLookupUsecase
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor

class YoutubeSearchPlayistInteractor constructor(
    private val prefsWrapper: MultiPlatformPreferencesWrapper,
    private val ytInteractor: YoutubeInteractor,
    private val playlistMediaLookupUsecase: PlaylistMediaLookupUsecase,
    private val state: State,
    private val guidCreator: GuidCreator,
) : AppPlaylistInteractor {

    override val hasCustomDeleteAction = false
    override val customResources = null

    private val YoutubeSearchIdentifier = YoutubeSearch.id.toIdentifier(MEMORY)

    data class State constructor(
        var playlist: PlaylistDomain? = null,
        var searchTerm: SearchRemoteDomain? = null
    )

    fun searchPref(): SearchRemoteDomain? = prefsWrapper.lastRemoteSearch

    override suspend fun getPlaylist(): PlaylistDomain? =
        cachedOrSearch()
            ?.let { playlistMediaLookupUsecase.lookupPlaylistItemsAndReplace(it) }
            ?.items
            ?.let {
                makeHeader()
                    .copy(items = it.mapIndexed { _, playlistItem -> playlistItem.copy() } /*i * 1000L*/)
            }

    suspend fun cachedOrSearch(): PlaylistDomain? =
        searchPref()
            ?.let { searchTerm ->
                if (state.playlist != null && state.searchTerm == searchTerm) {
                    state.playlist!!
                } else {
                    ytInteractor.search(searchTerm)
                        .takeIf { it.isSuccessful }
                        ?.data
                        ?.let {
                            it.copy(items = it.items.map { item ->
                                item.playlistId
                                    ?.let { item }
                                    ?: item.copy(
                                        id = guidCreator.create().toIdentifier(MEMORY),
                                        media = item.media.copy(id = guidCreator.create().toIdentifier(MEMORY)),
                                        playlistId = YoutubeSearchIdentifier
                                    )
                            })
                        }
                        ?.also {
                            state.playlist = it
                            state.searchTerm = searchTerm
                        }
                }
            }

    override fun makeHeader(): PlaylistDomain = PlaylistDomain(
        id = YoutubeSearchIdentifier,
        title = mapTitle(),
        type = APP,
        currentIndex = -1,
        starred = true,
        image = ImageDomain(url = "https://cuer-275020.web.app/images/headers/telescope-122960_640.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(
            playable = true,
            editable = false,
            deletableItems = false
        )
    )

    private fun mapTitle() = searchPref()?.let {
        if (it.relatedToMediaPlatformId != null) {
            "Related: ${it.relatedToMediaTitle}"
        } else {
            "Youtube Search: " + it.text
        }
    } ?: "No Remote - shouldn't see this"

    override fun makeStats(): PlaylistStatDomain = PlaylistStatDomain(
        playlistId = YoutubeSearchIdentifier,
        itemCount = -1,
        watchedItemCount = -1
    )

    fun clearCached() {
        state.playlist = null
        state.searchTerm = null
    }

    override suspend fun performCustomDeleteAction(item: PlaylistItemDomain) = Unit
}