package uk.co.sentinelweb.cuer.app.orchestrator.memory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.*
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.GUID
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
    private val starredItemsInteractor: StarredItemsPlayistInteractor,
    private val unfinishedItemsInteractor: UnfinishedItemsPlayistInteractor,
    private val remoteSearchOrchestrator: YoutubeSearchPlayistInteractor
) : MemoryRepository<PlaylistDomain> {

    enum class MemoryPlaylist(val id: GUID) {
        Shared(GUID("1ae0c7a4-0ae2-45fd-b90b-8e7a142c20d8")),
        NewItems(GUID("c7c6a812-0f39-496f-b769-a3c6606e4773")),
        Recent(GUID("9636c334-4219-4179-8c87-d3dc3fa8ebf8")),
        LocalSearch(GUID("aa4f2c64-bc51-4fab-8846-78093fd2562a")),
        YoutubeSearch(GUID("9ac7d9bf-32c1-4efe-a2a2-d0cd9ad6263b")),
        Starred(GUID("f54f43f7-3ad8-43cb-99f7-115d7868b20b")),
        Unfinished(GUID("0430c6c0-5153-4910-8306-562f21d6bbcc"));

        fun identifier() = Identifier(id, Source.MEMORY)
    }

    private val data: MutableMap<GUID, PlaylistDomain> = mutableMapOf()

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

    override suspend fun load(id: GUID, options: Options): PlaylistDomain? = when (id) {
        NewItems.id -> newItemsInteractor.getPlaylist()
        Recent.id -> recentItemsInteractor.getPlaylist()
        LocalSearch.id -> localSearchInteractor.getPlaylist()
        YoutubeSearch.id -> remoteSearchOrchestrator.getPlaylist()
        Starred.id -> starredItemsInteractor.getPlaylist()
        Unfinished.id -> unfinishedItemsInteractor.getPlaylist()
        Shared.id -> data[id]
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
            if (!options.flat || !data.containsKey(playlistId.id)) {
                domain.copy(
                    items = domain.items.map { item ->
                        item.copy(playlistId = playlistId)
                    }
                )
            } else {
                domain.copy(
                    items = data[playlistId.id]?.items
                        ?: throw IllegalStateException("Data got emptied")
                )
            }
        }?.also { data[it.id!!.id] = it }
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
            ?.let { data.remove(it.id) }
            .let { it != null }

    inner class PlayListItemMemoryRepository : MemoryRepository<PlaylistItemDomain> {
        private val _playlistItemFlow = MutableSharedFlow<Pair<Operation, PlaylistItemDomain>>()
        override val updates: Flow<Pair<Operation, PlaylistItemDomain>>
            get() = _playlistItemFlow

        override fun load(platformId: String, options: Options): PlaylistItemDomain? {
            throw NotImplementedException()
        }

        override fun load(domain: PlaylistItemDomain, options: Options): PlaylistItemDomain? {
            throw NotImplementedException()
        }

        override suspend fun load(id: GUID, options: Options): PlaylistItemDomain? {
            throw NotImplementedException()
        }

        override fun loadList(filter: Filter, options: Options): List<PlaylistItemDomain> =
            when (filter) {
                is MediaIdListFilter -> data.values
                    .map { it.items }
                    .flatten()
                    .filter { filter.ids.contains(it.media.id?.id) }
                else -> throw NotImplementedException()
            }

        override fun save(domain: PlaylistItemDomain, options: Options): PlaylistItemDomain =
            domain.playlistId
                ?.takeIf { data.contains(it.id) }
                ?.let { playlistId ->
                    data[playlistId.id]?.let {
                        data.set(playlistId.id, it.replaceItemByPlatformId(domain))
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
                ?.takeIf { data.contains(it.id) }
                ?.let { playlistId ->
                    data[playlistId.id]?.let {
                        data.set(playlistId.id, it.removeItemByPlatformId(domain) ?: it)
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
        private val _mediaFlow = MutableSharedFlow<Pair<Operation, MediaDomain>>()
        override val updates: Flow<Pair<Operation, MediaDomain>>
            get() = _mediaFlow

        override fun load(platformId: String, options: Options): MediaDomain? {
            throw NotImplementedException()
        }

        override fun load(domain: MediaDomain, options: Options): MediaDomain? {
            throw NotImplementedException()
        }

        override suspend fun load(id: GUID, options: Options): MediaDomain? {
            throw NotImplementedException()
        }

        override fun loadList(filter: Filter, options: Options): List<MediaDomain> {
            throw NotImplementedException()
        }

        override fun save(domain: MediaDomain, options: Options): MediaDomain {
            if (domain.id == null) {
                data[Shared.id]
                    .takeIf { it != null }
                    ?.also { data[Shared.id] = it.replaceMediaByPlatformId(media = domain) }
            }
            return domain
        }

        override fun save(domains: List<MediaDomain>, options: Options): List<MediaDomain> {
            data[Shared.id]
                .takeIf { it != null }
                ?.also { playlist ->
                    var playlistMutate = playlist
                    domains.forEach { domain ->
                        if (domain.id == null) {
                            playlistMutate = playlistMutate.replaceMediaByPlatformId(media = domain)
                        }
                    }
                    data[Shared.id] = playlistMutate
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
}