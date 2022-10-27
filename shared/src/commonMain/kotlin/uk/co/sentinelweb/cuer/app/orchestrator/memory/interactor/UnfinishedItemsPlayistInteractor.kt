package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.db.repository.PlaylistItemDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.UNFINISHED_PLAYLIST
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain

class UnfinishedItemsPlayistInteractor constructor(
    private val playlistItemDatabaseRepository: PlaylistItemDatabaseRepository,
) : AppPlaylistInteractor {
    override suspend fun getPlaylist(): PlaylistDomain? =
        playlistItemDatabaseRepository
            .loadList(OrchestratorContract.UnfinishedMediaFilter(20, 60, 300))
            .takeIf { it.isSuccessful }
            ?.data
            ?.let {
                makeHeader()
                    .copy(items = it.mapIndexed { _, playlistItem -> playlistItem.copy() })
            }

    override fun makeHeader(): PlaylistDomain = PlaylistDomain(
        id = UNFINISHED_PLAYLIST,
        title = "Unfinished",
        type = APP,
        currentIndex = -1,
        starred = true,
        image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/florencia-viadana-crop-RqRCejwnccw-unsplash.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(playable = false, editable = false, deletable = false)
    )

    override fun makeStats(): PlaylistStatDomain = PlaylistStatDomain(
        playlistId = UNFINISHED_PLAYLIST,
        itemCount = -1, // todo log in a background process and save to pref
        watchedItemCount = -1 // todo log in a background process and save to pref
    )
}