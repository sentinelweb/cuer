package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import uk.co.sentinelweb.cuer.app.db.repository.RoomPlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository
import uk.co.sentinelweb.cuer.core.ntuple.then
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor

class PlaylistOrchestrator constructor(
    private val roomPlaylistDatabaseRepository: RoomPlaylistDatabaseRepository,
    private val playlistMemoryRepository: PlaylistMemoryRepository,
    private val mediaOrchestrator: MediaOrchestrator,
    private val ytInteractor: YoutubeInteractor
) : OrchestratorContract<PlaylistDomain> {

    override val updates: Flow<Triple<Operation, Source, PlaylistDomain>>
        get() = merge(
            roomPlaylistDatabaseRepository.updates
                .map { it.first to LOCAL then it.second },
            (playlistMemoryRepository.updates
                .map { it.first to MEMORY then it.second })
        )

    suspend override fun load(id: Long, options: Options): PlaylistDomain? = when (options.source) {
        MEMORY -> playlistMemoryRepository.load(id, options)
        LOCAL -> roomPlaylistDatabaseRepository.load(id)
            .forceDatabaseSuccess()
        LOCAL_NETWORK -> throw NotImplementedException()
        REMOTE -> throw NotImplementedException()
        PLATFORM -> throw InvalidOperationException(this::class, null, options)
    }

    suspend override fun loadList(filter: Filter, options: Options): List<PlaylistDomain> =
        when (options.source) {
            MEMORY -> playlistMemoryRepository.loadList(filter, options)
            LOCAL -> roomPlaylistDatabaseRepository.loadList(filter, options.flat)
                .allowDatabaseListResultEmpty()
            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM -> throw InvalidOperationException(this::class, filter, options)
        }

    suspend override fun load(platformId: String, options: Options): PlaylistDomain? =
        when (options.source) {
            MEMORY -> playlistMemoryRepository.loadList(PlatformIdListFilter(listOf(platformId)), options)
                .firstOrNull()
            LOCAL -> roomPlaylistDatabaseRepository.loadList(PlatformIdListFilter(listOf(platformId)))
                .allowDatabaseListResultEmpty()
                .firstOrNull()
            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM -> ytInteractor.playlist(platformId)
                .forceNetSuccessNotNull("Youtube ${platformId} does not exist")
        }

    suspend override fun load(domain: PlaylistDomain, options: Options): PlaylistDomain? {
        throw NotImplementedException()
    }

    suspend override fun save(domain: PlaylistDomain, options: Options): PlaylistDomain =
        when (options.source) {
            MEMORY -> playlistMemoryRepository.save(domain, options)
            LOCAL ->
                roomPlaylistDatabaseRepository.save(domain, options.flat, options.emit)
                    .forceDatabaseSuccessNotNull("Save failed ${domain.id}")
            LOCAL_NETWORK -> TODO()
            REMOTE -> TODO()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    suspend override fun save(domains: List<PlaylistDomain>, options: Options): List<PlaylistDomain> =
        when (options.source) {
            MEMORY -> throw NotImplementedException()
            LOCAL ->
                roomPlaylistDatabaseRepository.save(domains, options.flat, options.emit)
                    .forceDatabaseListResultNotEmpty("Save failed ${domains.map { it.id }}")
            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }


    override suspend fun count(filter: Filter, options: Options): Int =
        when (options.source) {
            MEMORY -> playlistMemoryRepository.count(filter, options)
            LOCAL ->
                roomPlaylistDatabaseRepository.count(filter)
                    .forceDatabaseSuccessNotNull("Count failed $filter")
            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    override suspend fun delete(domain: PlaylistDomain, options: Options): Boolean =
        when (options.source) {
            MEMORY -> playlistMemoryRepository.delete(domain, options)
            LOCAL ->
                roomPlaylistDatabaseRepository.delete(domain, options.emit)
                    .forceDatabaseSuccessNotNull("Delete failed ${domain.id}")
            LOCAL_NETWORK -> throw NotImplementedException()
            REMOTE -> throw NotImplementedException()
            PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    suspend fun getPlaylistOrDefault(playlistId: Long?, options: Options): Pair<PlaylistDomain, Source>? =
        when (options.source) {
            MEMORY ->
                playlistMemoryRepository.load(playlistId!!, options) // TODO FALLBACK OR ERROR?
                    ?.apply { delay(20) } // fixme: fucking menu items don't load in time sine memory is sequential
                    ?.let { it to MEMORY }
                    ?: roomPlaylistDatabaseRepository.getPlaylistOrDefault(null, options.flat)
                        ?.let { it to LOCAL }
            LOCAL ->
                roomPlaylistDatabaseRepository.getPlaylistOrDefault(playlistId, options.flat)
                    ?.let { it to LOCAL }
            else -> throw InvalidOperationException(this::class, null, options)
        }

    // todo make update db better
    suspend fun updateCurrentIndex(it: PlaylistDomain, options: Options): Boolean =
        when (options.source) {
            MEMORY -> true // todo : persist current item? for new
            LOCAL ->
                roomPlaylistDatabaseRepository.updateCurrentIndex(it, options.emit)
                    .forceDatabaseSuccessNotNull("Update did not succeed")

            else -> throw InvalidOperationException(this::class, null, options)
        }

    suspend fun updateMedia(playlist: PlaylistDomain, update: UpdateDomain<MediaDomain>, options: Options): MediaDomain? =
        when (options.source) {
            MEMORY -> if (playlist.type == APP) {
                mediaOrchestrator.update(update, options.copy(source = LOCAL))
            } else null
            LOCAL -> mediaOrchestrator.update(update, options)
            else -> throw InvalidOperationException(this::class, null, options)
        }

    override suspend fun update(update: UpdateDomain<PlaylistDomain>, options: Options): PlaylistDomain? {
        throw NotImplementedException()
    }

}