package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.db.repository.PlaylistItemDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.STAR_PLAYLIST
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain

class StarredItemsPlayistInteractor constructor(
    private val playlistItemDatabaseRepository: PlaylistItemDatabaseRepository,
) : AppPlaylistInteractor {
    override suspend fun getPlaylist(): PlaylistDomain? =
        playlistItemDatabaseRepository
            .loadList(OrchestratorContract.StarredMediaFilter(300))
            .takeIf { it.isSuccessful }
            ?.data
            ?.let {
                makeHeader()
                    .copy(items = it.mapIndexed { _, playlistItem -> playlistItem.copy() })
            }

    override fun makeHeader(): PlaylistDomain = PlaylistDomain(
        id = STAR_PLAYLIST,
        title = "Starred items",
        type = APP,
        currentIndex = -1,
        starred = true,
        image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/pixabay-star-640-wallpaper-ga4c7c7acf_640.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(playable = false, editable = false, deletable = false)
    )


    override fun makeStats(): PlaylistStatDomain = PlaylistStatDomain(
        playlistId = STAR_PLAYLIST,
        itemCount = -1, // todo log in a background process and save to pref
        watchedItemCount = -1 // todo log in a background process and save to pref
    )
}