package uk.co.sentinelweb.cuer.app.orchestrator.util

import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.RecentMediaFilter
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.RECENT_PLAYLIST
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP

class RecentItemsPlayistOrchestrator constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository
) {
    suspend fun getPlaylist(): PlaylistDomain? =
        playlistDatabaseRepository
            .loadPlaylistItems(RecentMediaFilter())
            .takeIf { it.isSuccessful }
            ?.data
            ?.let {
                makeRecentItemsHeader()
                    .copy(items = it.mapIndexed { i, playlistItem -> playlistItem.copy(i * 1000L) })
            }

    fun makeRecentItemsHeader(): PlaylistDomain = PlaylistDomain(
        id = RECENT_PLAYLIST,
        title = "Recent items",
        type = APP,
        currentIndex = -1,
        image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/pexels-ketut-subiyanto-4474038-600.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(playable = false, editable = false)
    )

}