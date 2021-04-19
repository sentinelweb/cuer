package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.REMOTE_SEARCH_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.util.PlaylistMediaLookupOrchestrator
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.LAST_REMOTE_SEARCH
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.SearchRemoteDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseSearchRemote
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor

class RemoteSearchPlayistOrchestrator constructor(
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>,
    private val ytInteractor: YoutubeInteractor,
    private val playlistMediaLookupOrchestrator: PlaylistMediaLookupOrchestrator
) {
    fun search(): SearchRemoteDomain? =
        prefsWrapper
            .getString(LAST_REMOTE_SEARCH, null)
            ?.let { deserialiseSearchRemote(it) }

    suspend fun getPlaylist(): PlaylistDomain? =
        search()
            ?.let {
                ytInteractor.search(it)
                    .takeIf { it.isSuccessful }
                    ?.data
                    ?.let { playlistMediaLookupOrchestrator.lookupPlaylistItemsAndReplace(it) }
                    ?.items
                    ?.let {
                        makeSearchHeader()
                            .copy(
                                items = it.mapIndexed { i, playlistItem -> playlistItem.copy(i * 1000L) }
                            )
                    }
            }


    fun makeSearchHeader(): PlaylistDomain = PlaylistDomain(
        id = REMOTE_SEARCH_PLAYLIST,
        title = mapTitle(),
        type = APP,
        currentIndex = -1,
        starred = true,
        image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/telescope-122960_640.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(playable = false, editable = false)
    )

    private fun mapTitle() =
        "Remote Search: " + search()?.text

    fun makeSearchItemsStats(): PlaylistStatDomain = PlaylistStatDomain(
        playlistId = REMOTE_SEARCH_PLAYLIST,
        itemCount = -1,
        watchedItemCount = -1
    )
}