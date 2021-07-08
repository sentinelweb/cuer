package uk.co.sentinelweb.cuer.app.orchestrator.util

import kotlinx.coroutines.delay
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.forceDatabaseSuccessNotNull
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.update.PlaylistIndexUpdateDomain

class PlaylistOrDefaultOrchestrator constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository,
    private val playlistMemoryRepository: PlaylistMemoryRepository,
) {
    suspend fun getPlaylistOrDefault(
        playlistId: Long?,
        options: OrchestratorContract.Options,
    ): Pair<PlaylistDomain, OrchestratorContract.Source>? =
        when (options.source) {
            OrchestratorContract.Source.MEMORY ->
                playlistMemoryRepository.load(playlistId!!, options) // TODO FALLBACK OR ERROR?
                    ?.apply { delay(20) } // fixme: fucking menu items don't load in time sine memory is sequential
                    ?.let { it to OrchestratorContract.Source.MEMORY }
                    ?: getPlaylistOrDefault(null, options.flat)
                        ?.let { it to OrchestratorContract.Source.LOCAL }
            OrchestratorContract.Source.LOCAL ->
                getPlaylistOrDefault(playlistId, options.flat)
                    ?.let { it to OrchestratorContract.Source.LOCAL }
            else -> throw UnsupportedOperationException("Not supported for ${options.source}")
        }

    private suspend fun getPlaylistOrDefault(playlistId: Long?, flat: Boolean = false) =
        (playlistId
            ?.takeIf { it >= 0 }
            ?.let { playlistDatabaseRepository.load(it, flat = false) }
            ?.takeIf { it.isSuccessful }
            ?.data
            ?: run {
                playlistDatabaseRepository.loadList(OrchestratorContract.DefaultFilter(), flat)
                    .takeIf { it.isSuccessful && it.data?.size ?: 0 > 0 }
                    ?.data?.get(0)
            })

    suspend fun updateCurrentIndex(input: PlaylistDomain, options: OrchestratorContract.Options): Boolean =
        when (options.source) {
            OrchestratorContract.Source.MEMORY -> true // todo : persist current item? for new
            OrchestratorContract.Source.LOCAL ->
                playlistDatabaseRepository.update(PlaylistIndexUpdateDomain(input.id!!, input.currentIndex), options.emit)
                    .forceDatabaseSuccessNotNull("Update did not succeed")
                    .let { it.currentIndex == input.currentIndex }
            else -> throw UnsupportedOperationException("Not supported for ${options.source}")
        }
}