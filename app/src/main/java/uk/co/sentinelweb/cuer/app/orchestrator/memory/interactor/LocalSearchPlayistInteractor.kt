package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.LOCAL_SEARCH_PLAYLIST
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.SearchLocalDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseSearchLocal

class LocalSearchPlayistInteractor constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>
) {
    fun search(): SearchLocalDomain? =
        prefsWrapper
            .getString(GeneralPreferences.LAST_LOCAL_SEARCH, null)
            ?.let { deserialiseSearchLocal(it) }

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

    private fun mapToFilter(searchDomain: SearchLocalDomain) = OrchestratorContract.SearchFilter(
        text = searchDomain.text,
        isWatched = searchDomain.isWatched,
        isNew = searchDomain.isNew,
        isLive = searchDomain.isLive,
        playlistIds = if (searchDomain.playlists.isEmpty()) null
        else searchDomain.playlists.mapNotNull { it.id }
    )

    fun makeSearchHeader(): PlaylistDomain = PlaylistDomain(
        id = LOCAL_SEARCH_PLAYLIST,
        title = mapTitle(),
        type = APP,
        currentIndex = -1,
        starred = true,
        image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/pexels-noelle-otto-906055.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(playable = false, editable = false)
    )

    private fun mapTitle() =
        "Local Search: " +
                search()?.let {
                    it.text + it.playlists.let {
                        if (it.isNotEmpty()) " " + it.map { it.title } else ""
                    }
                }


    fun makeSearchItemsStats(): PlaylistStatDomain = PlaylistStatDomain(
        playlistId = LOCAL_SEARCH_PLAYLIST,
        itemCount = -1,
        watchedItemCount = -1
    )
}