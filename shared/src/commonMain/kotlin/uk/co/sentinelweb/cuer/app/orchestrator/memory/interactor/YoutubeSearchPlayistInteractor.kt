package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.YoutubeSearch
import uk.co.sentinelweb.cuer.app.orchestrator.util.PlaylistMediaLookupOrchestrator
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.LAST_REMOTE_SEARCH
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.ext.deserialiseSearchRemote
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor

class YoutubeSearchPlayistInteractor constructor(
    private val prefsWrapper: GeneralPreferencesWrapper,
    private val ytInteractor: YoutubeInteractor,
    private val playlistMediaLookupOrchestrator: PlaylistMediaLookupOrchestrator,
    private val state: State
) : AppPlaylistInteractor {

    override val hasCustomDeleteAction = false
    override val customResources = null

    data class State constructor(
        var playlist: PlaylistDomain? = null,
        var searchTerm: SearchRemoteDomain? = null
    )

    fun searchPref(): SearchRemoteDomain? =
        prefsWrapper
            .getString(LAST_REMOTE_SEARCH, null)
            ?.let { deserialiseSearchRemote(it) }

    override suspend fun getPlaylist(): PlaylistDomain? =
        cachedOrSearch()
            ?.let { playlistMediaLookupOrchestrator.lookupPlaylistItemsAndReplace(it) }
            ?.items
            ?.let {
                makeHeader()
                    .copy(
                        items = it.mapIndexed { _, playlistItem -> playlistItem.copy() } //i * 1000L
                    )
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
                        ?.also {
                            state.playlist = it
                            state.searchTerm = searchTerm
                        }
                }
            }

    override fun makeHeader(): PlaylistDomain = PlaylistDomain(
        id = YoutubeSearch.id,
        title = mapTitle(),
        type = APP,
        currentIndex = -1,
        starred = true,
        image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/telescope-122960_640.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(
            playable = false,
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
        playlistId = YoutubeSearch.id,
        itemCount = -1,
        watchedItemCount = -1
    )

    fun clearCached() {
        state.playlist = null
        state.searchTerm = null
    }

    override suspend fun performCustomDeleteAction(item: PlaylistItemDomain) = Unit
}