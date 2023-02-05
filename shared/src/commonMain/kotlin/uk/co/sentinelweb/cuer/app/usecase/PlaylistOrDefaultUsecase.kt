package uk.co.sentinelweb.cuer.app.usecase

import kotlinx.coroutines.delay
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.DefaultFilter
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.orchestrator.forceDatabaseSuccessNotNull
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.update.PlaylistIndexUpdateDomain

class PlaylistOrDefaultUsecase constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository,
    private val playlistMemoryRepository: PlaylistMemoryRepository,
) {
//    suspend fun getPlaylistOrDefault(id: OrchestratorContract.Identifier<GUID>?): PlaylistDomain? =
//        id?.let { getPlaylistOrDefault(it.id) }

    suspend fun getPlaylistOrDefault(
        playlistId: OrchestratorContract.Identifier<GUID>?,
    ): PlaylistDomain? =
        when (playlistId?.source) {
            OrchestratorContract.Source.MEMORY ->
                playlistId.id
                    .takeIf { it != OrchestratorContract.NO_PLAYLIST.id }
                    ?.let { playlistMemoryRepository.load(it, playlistId.source.deepOptions()) }
                    ?.apply { delay(20) } // fixme: menu items don't load in time since memory is sequential
                    ?: getPlaylistOrDefaultInternal(null)

            OrchestratorContract.Source.LOCAL -> getPlaylistOrDefaultInternal(playlistId)
            else -> throw UnsupportedOperationException("Not supported: playlistId: $playlistId")
        }

    private suspend fun getPlaylistOrDefaultInternal(playlistId: OrchestratorContract.Identifier<GUID>?): PlaylistDomain? =
        (playlistId
            ?.let { playlistDatabaseRepository.load(it.id, flat = false) }
            ?.takeIf { it.isSuccessful }
            ?.data
            ?: run {
                playlistDatabaseRepository.loadList(DefaultFilter, false)
                    .takeIf { it.isSuccessful && (it.data?.size ?: 0) > 0 }
                    ?.data?.get(0)
            })

    suspend fun updateCurrentIndex(input: PlaylistDomain, options: OrchestratorContract.Options): Boolean =
        when (options.source) {
            OrchestratorContract.Source.MEMORY -> true
            OrchestratorContract.Source.LOCAL ->
                playlistDatabaseRepository.update(
                    PlaylistIndexUpdateDomain(input.id!!, input.currentIndex),
                    emit = options.emit,
                    flat = false
                ).forceDatabaseSuccessNotNull("Update did not succeed")
                    .let { it.currentIndex == input.currentIndex }

            else -> throw UnsupportedOperationException("Not supported for ${options.source}")
        }
}