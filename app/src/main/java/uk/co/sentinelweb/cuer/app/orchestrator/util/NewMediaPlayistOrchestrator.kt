package uk.co.sentinelweb.cuer.app.orchestrator.util

import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.NewMediaFilter
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.NEWITEMS_PLAYLIST
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP

class NewMediaPlayistOrchestrator constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository
) {
    suspend fun getPlaylist(): PlaylistDomain? =
        playlistDatabaseRepository
            .loadPlaylistItems(NewMediaFilter())
            .takeIf { it.isSuccessful }
            ?.data
            ?.let {
                makeNewItemsHeader()
                    .copy(items = it.mapIndexed { i, playlistItem -> playlistItem.copy(i * 1000L) })
            }

    fun makeNewItemsHeader(): PlaylistDomain = PlaylistDomain(
        id = NEWITEMS_PLAYLIST,
        title = "New items",
        type = APP,
        currentIndex = -1,
        image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/pexels-pixabay-40663-600.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(playable = false, editable = false)
    )

}