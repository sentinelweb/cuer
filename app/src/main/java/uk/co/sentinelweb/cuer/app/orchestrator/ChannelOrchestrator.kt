package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor

class ChannelOrchestrator constructor(
    private val mediaDatabaseRepository: MediaDatabaseRepository,
    private val ytInteractor: YoutubeInteractor
) : OrchestratorContract<ChannelDomain> {
    override val updates: Flow<Pair<OrchestratorContract.Operation, ChannelDomain>>
        get() = TODO("Not yet implemented")

    suspend override fun load(id: Long, options: OrchestratorContract.Options): ChannelDomain? {
        TODO("Not yet implemented")
    }

    suspend override fun loadList(filter: OrchestratorContract.Filter, options: OrchestratorContract.Options): List<ChannelDomain> {
        TODO("Not yet implemented")
    }

    suspend override fun load(platformId: String, options: OrchestratorContract.Options): ChannelDomain? {
        TODO("Not yet implemented")
    }

    suspend override fun load(domain: ChannelDomain, options: OrchestratorContract.Options): ChannelDomain? {
        TODO("Not yet implemented")
    }

    suspend override fun save(domain: ChannelDomain, options: OrchestratorContract.Options): ChannelDomain {
        TODO("Not yet implemented")
    }

    suspend override fun save(domains: List<ChannelDomain>, options: OrchestratorContract.Options): List<ChannelDomain> {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: OrchestratorContract.Filter, options: OrchestratorContract.Options): Int {
        TODO("Not yet implemented")
    }

    override suspend fun delete(domain: ChannelDomain, options: OrchestratorContract.Options): Boolean {
        TODO("Not yet implemented")
    }


}