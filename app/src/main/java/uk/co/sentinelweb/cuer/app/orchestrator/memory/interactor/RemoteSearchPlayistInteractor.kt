package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.REMOTE_SEARCH_PLAYLIST
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.LAST_LOCAL_SEARCH
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.SearchDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseSearch

class RemoteSearchPlayistInteractor constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>
) {
    fun search(): SearchDomain? =
        prefsWrapper
            .getString(LAST_LOCAL_SEARCH, null)
            ?.let { deserialiseSearch(it) }

    suspend fun getPlaylist(): PlaylistDomain? =
        search()
            ?.let { mapToFilter(it) }
            ?.let {
                playlistDatabaseRepository
                    .loadPlaylistItems(it)
                    .takeIf { it.isSuccessful }
                    ?.data
                    ?.let {
                        makeSearchHeader()
                            .copy(items = it.mapIndexed { i, playlistItem -> playlistItem.copy(i * 1000L) })
                    }
            }

    private fun mapToFilter(searchDomain: SearchDomain) = OrchestratorContract.SearchFilter(
        text = searchDomain.localParams.text,
        isWatched = searchDomain.localParams.isWatched,
        isNew = searchDomain.localParams.isNew,
        isLive = searchDomain.localParams.isLive,
        playlistIds = if (searchDomain.localParams.playlists.isEmpty()) null
        else searchDomain.localParams.playlists.mapNotNull { it.id }
    )

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
        "Remote Search: " +
                search()?.let {
                    it.localParams.text + it.localParams.playlists.let {
                        if (it.isNotEmpty()) " " + it.map { it.title } else ""
                    }
                }

    fun makeSearchItemsStats(): PlaylistStatDomain = PlaylistStatDomain(
        playlistId = REMOTE_SEARCH_PLAYLIST,
        itemCount = -1,
        watchedItemCount = -1
    )
}