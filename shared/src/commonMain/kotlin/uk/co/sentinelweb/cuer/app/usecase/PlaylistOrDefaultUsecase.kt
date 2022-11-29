package uk.co.sentinelweb.cuer.app.usecase

import kotlinx.coroutines.delay
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.DefaultFilter
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.orchestrator.forceDatabaseSuccessNotNull
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.update.PlaylistIndexUpdateDomain

class PlaylistOrDefaultUsecase constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository,
    private val playlistMemoryRepository: PlaylistMemoryRepository,
) {
    suspend fun getPlaylistOrDefault(id: OrchestratorContract.Identifier<*>?)
            : Pair<PlaylistDomain, OrchestratorContract.Source>? =
        id?.let { getPlaylistOrDefault(it.id as Long, it.source.flatOptions()) }

    suspend fun getPlaylistOrDefault(
        playlistId: Long?,
        options: OrchestratorContract.Options,
    ): Pair<PlaylistDomain, OrchestratorContract.Source>? =
        when (options.source) {
            OrchestratorContract.Source.MEMORY ->
                playlistMemoryRepository.load(playlistId!!, options) // TODO FALLBACK OR ERROR?
                    ?.apply { delay(20) } // fixme: menu items don't load in time sine memory is sequential
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
                playlistDatabaseRepository.loadList(DefaultFilter, flat)
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