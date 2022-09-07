package uk.co.sentinelweb.cuer.app.orchestrator.memory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.LocalSearchPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.NewMediaPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.RecentItemsPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.RemoteSearchPlayistOrchestrator
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.removeItemByPlatformId
import uk.co.sentinelweb.cuer.domain.ext.replaceItemByPlatformId
import uk.co.sentinelweb.cuer.domain.ext.replaceMediaByPlatformId

class PlaylistMemoryRepository constructor(
    private val coroutines: CoroutineContextProvider,
    private val newItemsInteractor: NewMediaPlayistInteractor,
    private val recentItemsInteractor: RecentItemsPlayistInteractor,
    private val localSearchInteractor: LocalSearchPlayistInteractor,
    private val remoteSearchOrchestrator: RemoteSearchPlayistOrchestrator
) : MemoryRepository<PlaylistDomain> {

    private val data: MutableMap<Long, PlaylistDomain> = mutableMapOf()

    val playlistItemMemoryRepository = PlayListItemMemoryRepository()
    val mediaMemoryRepository = MediaMemoryRepository()

    private val _playlistFlow = MutableSharedFlow<Pair<Operation, PlaylistDomain>>()
    override val updates: Flow<Pair<Operation, PlaylistDomain>>
        get() = _playlistFlow

    override fun load(platformId: String, options: Options): PlaylistDomain? {
        throw NotImplementedException()
    }

    override fun load(domain: PlaylistDomain, options: Options): PlaylistDomain? {
        throw NotImplementedException()
    }

    override suspend fun load(id: Long, options: Options): PlaylistDomain? = when (id) {
        NEWITEMS_PLAYLIST -> newItemsInteractor.getPlaylist()
        RECENT_PLAYLIST -> recentItemsInteractor.getPlaylist()
        LOCAL_SEARCH_PLAYLIST -> localSearchInteractor.getPlaylist()
        REMOTE_SEARCH_PLAYLIST -> remoteSearchOrchestrator.getPlaylist()
        SHARED_PLAYLIST -> data[id]
        else -> throw NotImplementedException("$id is invalid memory playlist")
    }

    override fun loadList(filter: Filter, options: Options): List<PlaylistDomain> = when (filter) {
        is IdListFilter -> data.keys
            .filter { filter.ids.contains(it) }
            .map { data[it] }
            .filterNotNull()

        else -> throw NotImplementedException("$filter")
    }

    override fun save(domain: PlaylistDomain, options: Options): PlaylistDomain =
        domain.id?.let { playlistId ->
            if (!options.flat || !data.containsKey(playlistId)) {
                domain.copy(// todo fix this in share
                    items = domain.items.mapIndexed { _, item ->
                        item.copy(
                            //id = playlistItemMemoryRepository.idCounter,
                            playlistId = playlistId,
                            //media = item.media.copy(id = playlistItemMemoryRepository.idCounter)
                        )
                    }
                )
            } else {
                domain.copy(
                    items = data[playlistId]?.items
                        ?: throw IllegalStateException("Data got emptied")
                )
            }
        }?.also { data[it.id!!] = it }
            ?.also {
                if (options.emit) {
                    coroutines.computationScope.launch {
                        _playlistFlow.emit((if (options.flat) FLAT else FULL) to domain)
                    }
                }
            } ?: throw MemoryException("Please set the ID")

    override fun save(domains: List<PlaylistDomain>, options: Options): List<PlaylistDomain> {
        throw NotImplementedException()
    }

    override fun count(filter: Filter, options: Options): Int = when (filter) {
        is PlatformIdListFilter -> data.values.filter { filter.ids.contains(it.platformId) }.size
        else -> throw NotImplementedException()
    }

    override fun delete(domain: PlaylistDomain, options: Options): Boolean =
        domain.id
            ?.let { data.remove(it) }
            .let { it != null }

    inner class PlayListItemMemoryRepository : MemoryRepository<PlaylistItemDomain> {
        private val _playlistItemFlow = MutableSharedFlow<Pair<Operation, PlaylistItemDomain>>()
        override val updates: Flow<Pair<Operation, PlaylistItemDomain>>
            get() = _playlistItemFlow

//        private var _idCounter = 0L
//        val idCounter: Long
//            get() {
//                _idCounter--
//                return _idCounter
//            }

        override fun load(platformId: String, options: Options): PlaylistItemDomain? {
            throw NotImplementedException()
        }

        override fun load(domain: PlaylistItemDomain, options: Options): PlaylistItemDomain? {
            throw NotImplementedException()
        }

        override suspend fun load(id: Long, options: Options): PlaylistItemDomain? {
            throw NotImplementedException()
        }

        override fun loadList(filter: Filter, options: Options): List<PlaylistItemDomain> =
            when (filter) {
                is MediaIdListFilter -> data.values
                    .map { it.items }
                    .flatten()
                    .filter { filter.ids.contains(it.media.id) }
                else -> throw NotImplementedException()
            }

        override fun save(domain: PlaylistItemDomain, options: Options): PlaylistItemDomain =
            domain.playlistId
                ?.takeIf { data.contains(it) }
                ?.let { playlistId ->
                    data[playlistId]?.let {
                        data.set(playlistId, it.replaceItemByPlatformId(domain))
                        if (options.emit) {
                            coroutines.computationScope.launch {
                                _playlistItemFlow.emit((if (options.flat) FLAT else FULL) to domain)
                            }
                        }
                        domain
                    }
                } ?: throw DoesNotExistException("Playlist ${domain.playlistId} does not exist")

        override fun save(
            domains: List<PlaylistItemDomain>,
            options: Options
        ): List<PlaylistItemDomain> {
            throw NotImplementedException()
        }

        override fun count(filter: Filter, options: Options): Int {
            throw NotImplementedException()
        }

        override fun delete(domain: PlaylistItemDomain, options: Options): Boolean =
            domain.playlistId
                ?.takeIf { data.contains(it) }
                ?.let { playlistId ->
                    data[playlistId]?.let {
                        data.set(playlistId, it.removeItemByPlatformId(domain) ?: it)
                        if (options.emit) {
                            coroutines.computationScope.launch {
                                _playlistItemFlow.emit(DELETE to domain)
                            }
                        }
                        true
                    }
                    false
                } ?: false
    }

    inner class MediaMemoryRepository : MemoryRepository<MediaDomain> {
        private val _playlistItemFlow = MutableSharedFlow<Pair<Operation, MediaDomain>>()
        override val updates: Flow<Pair<Operation, MediaDomain>>
            get() = _playlistItemFlow

        override fun load(platformId: String, options: Options): MediaDomain? {
            throw NotImplementedException()
        }

        override fun load(domain: MediaDomain, options: Options): MediaDomain? {
            throw NotImplementedException()
        }

        override suspend fun load(id: Long, options: Options): MediaDomain? {
            throw NotImplementedException()
        }

        override fun loadList(filter: Filter, options: Options): List<MediaDomain> {
            throw NotImplementedException()
        }

        override fun save(domain: MediaDomain, options: Options): MediaDomain {
            if (domain.id == null) {
                data[SHARED_PLAYLIST]
                    .takeIf { it != null }
                    ?.also { data[SHARED_PLAYLIST] = it.replaceMediaByPlatformId(media = domain) }
            }
            return domain
        }

        override fun save(domains: List<MediaDomain>, options: Options): List<MediaDomain> {
            data[SHARED_PLAYLIST]
                .takeIf { it != null }
                ?.also { playlist ->
                    var playlistMutate = playlist
                    domains.forEach { domain ->
                        if (domain.id == null) {
                            playlistMutate = playlistMutate.replaceMediaByPlatformId(media = domain)
                        }
                    }
                    data[SHARED_PLAYLIST] = playlistMutate
                }
            return domains
        }

        override fun count(filter: Filter, options: Options): Int {
            throw NotImplementedException()
        }

        override fun delete(domain: MediaDomain, options: Options): Boolean {
            throw NotImplementedException()
        }
    }

    companion object {
        const val SHARED_PLAYLIST: Long = -100
        const val NEWITEMS_PLAYLIST: Long = -101
        const val RECENT_PLAYLIST: Long = -102
        const val LOCAL_SEARCH_PLAYLIST: Long = -103
        const val REMOTE_SEARCH_PLAYLIST: Long = -104
    }
}