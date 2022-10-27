package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.YOUTUBE_SEARCH_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.util.PlaylistMediaLookupOrchestrator
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.LAST_REMOTE_SEARCH
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.SearchRemoteDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseSearchRemote
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor

class YoutubeSearchPlayistOrchestrator constructor(
    private val prefsWrapper: GeneralPreferencesWrapper,
    private val ytInteractor: YoutubeInteractor,
    private val playlistMediaLookupOrchestrator: PlaylistMediaLookupOrchestrator,
    private val state: State
) {
    data class State constructor(
        var playlist: PlaylistDomain? = null,
        var searchTerm: SearchRemoteDomain? = null
    )

    fun searchPref(): SearchRemoteDomain? =
        prefsWrapper
            .getString(LAST_REMOTE_SEARCH, null)
            ?.let { deserialiseSearchRemote(it) }

    suspend fun getPlaylist(): PlaylistDomain? =
        cachedOrSearch()
            ?.let { playlistMediaLookupOrchestrator.lookupPlaylistItemsAndReplace(it) }
            ?.items
            ?.let {
                makeSearchHeader()
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

    fun makeSearchHeader(): PlaylistDomain = PlaylistDomain(
        id = YOUTUBE_SEARCH_PLAYLIST,
        title = mapTitle(),
        type = APP,
        currentIndex = -1,
        starred = true,
        image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/telescope-122960_640.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(playable = false, editable = false, deletableItems = false)
    )

    private fun mapTitle() = searchPref()?.let {
        if (it.relatedToMediaPlatformId != null) {
            "Related: ${it.relatedToMediaTitle}"
        } else {
            "Youtube Search: " + it.text
        }
    } ?: "No Remote - shouldn't see this"

    fun makeSearchItemsStats(): PlaylistStatDomain = PlaylistStatDomain(
        playlistId = YOUTUBE_SEARCH_PLAYLIST,
        itemCount = -1,
        watchedItemCount = -1
    )

    fun clearCached() {
        state.playlist = null
        state.searchTerm = null
    }
}