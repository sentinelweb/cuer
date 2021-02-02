package uk.co.sentinelweb.cuer.app.orchestrator.memory

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.NotImplementedException
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Options
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlaylistMemoryRepository : MemoryRepository<PlaylistDomain> {

    private val data: MutableMap<Long, PlaylistDomain> = mutableMapOf()

    val playlistItemMemoryRepository = PlayListItemMemoryRepository()

    override val updates: Flow<Pair<OrchestratorContract.Operation, PlaylistDomain>>
        get() = throw NotImplementedException()

    override fun load(platformId: String, options: OrchestratorContract.Options): PlaylistDomain? {
        throw NotImplementedException()
    }

    override fun load(domain: PlaylistDomain, options: Options): PlaylistDomain? {
        throw NotImplementedException()
    }

    override fun load(id: Long, options: Options): PlaylistDomain? = data[id]

    override fun loadList(filter: OrchestratorContract.Filter, options: Options): List<PlaylistDomain> {
        throw NotImplementedException()
    }

    override fun save(domain: PlaylistDomain, options: Options): PlaylistDomain =
        domain.id?.let { playlistId ->
            domain.copy(
                items = domain.items.mapIndexed { index, item -> item.copy(id = index.toLong(), playlistId = playlistId) }
            ).also { data[playlistId] = it }
        } ?: throw OrchestratorContract.MemoryException("Please set the ID")

    override fun save(domains: List<PlaylistDomain>, options: Options): List<PlaylistDomain> {
        throw NotImplementedException()
    }

    override fun count(filter: OrchestratorContract.Filter, options: Options): Int {
        throw NotImplementedException()
    }

    override fun delete(domain: PlaylistDomain, options: Options): Boolean {
        throw NotImplementedException()
    }

    inner class PlayListItemMemoryRepository : MemoryRepository<PlaylistItemDomain> {
        override val updates: Flow<Pair<OrchestratorContract.Operation, PlaylistItemDomain>>
            get() = throw NotImplementedException()

        override fun load(platformId: String, options: OrchestratorContract.Options): PlaylistItemDomain? {
            throw NotImplementedException()
        }

        override fun load(domain: PlaylistItemDomain, options: Options): PlaylistItemDomain? {
            throw NotImplementedException()
        }

        override fun load(id: Long, options: Options): PlaylistItemDomain? {
            throw NotImplementedException()
        }

        override fun loadList(filter: OrchestratorContract.Filter, options: Options): List<PlaylistItemDomain> {
            throw NotImplementedException()
        }

        override fun save(domain: PlaylistItemDomain, options: Options): PlaylistItemDomain {
            throw NotImplementedException()
        }

        override fun save(domains: List<PlaylistItemDomain>, options: Options): List<PlaylistItemDomain> {
            throw NotImplementedException()
        }

        override fun count(filter: OrchestratorContract.Filter, options: Options): Int {
            throw NotImplementedException()
        }

        override fun delete(domain: PlaylistItemDomain, options: Options): Boolean {
            throw NotImplementedException()
        }

    }

    companion object {
        val SHARED_PLAYLIST: Long = -100
        val NEWITEMS_PLAYLIST: Long = -101
    }
}