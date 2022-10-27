package uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor

import uk.co.sentinelweb.cuer.app.db.repository.PlaylistItemDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.NewMediaFilter
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.NEWITEMS_PLAYLIST
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain

class NewMediaPlayistInteractor constructor(
    private val playlistItemDatabaseRepository: PlaylistItemDatabaseRepository,
) {
    suspend fun getPlaylist(): PlaylistDomain? =
        playlistItemDatabaseRepository
            .loadList(NewMediaFilter(300))
            .takeIf { it.isSuccessful }
            ?.data
            ?.let {
                makeNewItemsHeader()
                    .copy(items = it.mapIndexed { _, playlistItem -> playlistItem.copy() })
            }

    fun makeNewItemsHeader(): PlaylistDomain = PlaylistDomain(
        id = NEWITEMS_PLAYLIST,
        title = "New",
        type = APP,
        currentIndex = -1,
        starred = true,
        image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/pexels-pixabay-40663-600.jpg"),
        config = PlaylistDomain.PlaylistConfigDomain(
            playable = false,
            editable = false,
            deletable = false,
            deletableItems = false
        )
    )

    fun makeNewItemsStats(): PlaylistStatDomain = PlaylistStatDomain(
        playlistId = NEWITEMS_PLAYLIST,
        itemCount = -1, // todo log in a background process and save to pref
        watchedItemCount = 0 // todo log in a background process and save to pref
    )

}