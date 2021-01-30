package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor

class PlaylistOrchestrator constructor(
    private val playlistDatabaseRepository: PlaylistDatabaseRepository,
    private val ytInteractor: YoutubeInteractor
) : OrchestratorContract<PlaylistDomain> {

    override val updates: Flow<Pair<Operation, PlaylistDomain>>
        get() = playlistDatabaseRepository.playlistFlow

    suspend override fun load(id: Long, options: Options): PlaylistDomain? = when (options.source) {
        MEMORY -> TODO()
        LOCAL -> TODO()
        LOCAL_NETWORK -> TODO()
        REMOTE -> TODO()
        PLATFORM -> throw InvalidOperationException(this::class, null, options)
    }

    suspend override fun loadList(filter: Filter, options: Options): List<PlaylistDomain> =
        when (options.source) {
            MEMORY -> TODO()
            LOCAL -> playlistDatabaseRepository.loadList(filter)
                .forceDatabaseListResultNotEmpty("Playlist $filter does not exist")
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, filter, options)
        }

    suspend override fun load(platformId: String, options: Options): PlaylistDomain? =
        when (options.source) {
            MEMORY -> TODO()
            LOCAL -> playlistDatabaseRepository.loadList(PlatformIdListFilter(listOf(platformId)))
                .forceDatabaseListResultNotEmpty("Playlist platformId = $platformId does not exist")
                .get(0)
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> ytInteractor.playlist(platformId)
                .forceNetResultNotNull("Youtube ${platformId} does not exist")
        }

    suspend override fun load(domain: PlaylistDomain, options: Options): PlaylistDomain? {
        TODO("Not yet implemented")
    }

    suspend override fun save(domain: PlaylistDomain, options: Options): PlaylistDomain =
        when (options.source) {
            MEMORY -> TODO()
            LOCAL ->
                playlistDatabaseRepository.save(domain, options.emit)
                    .forceDatabaseSuccess("Save failed ${domain.id}")
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    suspend override fun save(domains: List<PlaylistDomain>, options: Options): List<PlaylistDomain> =
        when (options.source) {
            MEMORY -> TODO()
            LOCAL ->
                playlistDatabaseRepository.save(domains, options.emit)
                    .forceDatabaseSuccess("Save failed ${domains.map { it.id }}")
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    override suspend fun count(filter: Filter, options: Options): Int =
        when (options.source) {
            MEMORY -> TODO()
            LOCAL ->
                playlistDatabaseRepository.count(filter)
                    .forceDatabaseSuccess("Count failed $filter")
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    override suspend fun delete(domain: PlaylistDomain, options: Options): Boolean =
        when (options.source) {
            MEMORY -> TODO()
            LOCAL ->
                playlistDatabaseRepository.delete(domain, options.emit)
                    .forceDatabaseSuccess("Delete failed ${domain.id}")
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    suspend fun getPlaylistOrDefault(playlistId: Long?, options: Options): PlaylistDomain? =
        when (options.source) {
            LOCAL ->
                playlistDatabaseRepository.getPlaylistOrDefault(playlistId, options.flat)
            else -> throw InvalidOperationException(this::class, null, options)
        }

    suspend fun updateCurrentIndex(it: PlaylistDomain, options: Options): Boolean =
        when (options.source) {
            LOCAL ->
                playlistDatabaseRepository.updateCurrentIndex(it, options.emit)
                    .isSuccessful
            else -> throw InvalidOperationException(this::class, null, options)
        }


}