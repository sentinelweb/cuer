package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.db.repository.RoomPlaylistItemDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.RecentMediaFilter
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.RECENT_PLAYLIST
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain

class RecentItemsPlayistInteractor constructor(
    private val roomPlaylistItemDatabaseRepository: RoomPlaylistItemDatabaseRepository,
) {
    suspend fun getPlaylist(): PlaylistDomain? =
        roomPlaylistItemDatabaseRepository
            .loadPlaylistItems(RecentMediaFilter())
            .takeIf { it.isSuccessful }
            ?.data
            ?.let {
                makeRecentItemsHeader()
                    .copy(items = it.mapIndexed { i, playlistItem -> playlistItem.copy() })
            }

    fun makeRecentItemsHeader(): PlaylistDomain = PlaylistDomain(
        id = RECENT_PLAYLIST,
        title = "Recent items",
        type = APP,
        currentIndex = -1,
        starred = true,
        image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/pexels-ketut-subiyanto-4474038-600.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(playable = false, editable = false, deletable = false)
    )


    fun makeRecentItemsStats(): PlaylistStatDomain = PlaylistStatDomain(
        playlistId = RECENT_PLAYLIST,
        itemCount = -1, // todo log in a background process and save to pref
        watchedItemCount = -1 // todo log in a background process and save to pref
    )
}