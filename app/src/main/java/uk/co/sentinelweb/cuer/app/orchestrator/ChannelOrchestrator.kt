package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateObject
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor

class ChannelOrchestrator constructor(
    private val mediaDatabaseRepository: MediaDatabaseRepository,
    private val ytInteractor: YoutubeInteractor
) : OrchestratorContract<ChannelDomain> {
    override val updates: Flow<Triple<Operation, Source, ChannelDomain>>
        get() = throw OrchestratorContract.NotImplementedException()

    suspend override fun load(id: Long, options: OrchestratorContract.Options): ChannelDomain? {
        throw OrchestratorContract.NotImplementedException()
    }

    suspend override fun loadList(filter: OrchestratorContract.Filter, options: OrchestratorContract.Options): List<ChannelDomain> {
        throw OrchestratorContract.NotImplementedException()
    }

    suspend override fun load(platformId: String, options: OrchestratorContract.Options): ChannelDomain? {
        throw OrchestratorContract.NotImplementedException()
    }

    suspend override fun load(domain: ChannelDomain, options: OrchestratorContract.Options): ChannelDomain? {
        throw OrchestratorContract.NotImplementedException()
    }

    suspend override fun save(domain: ChannelDomain, options: OrchestratorContract.Options): ChannelDomain {
        throw OrchestratorContract.NotImplementedException()
    }

    suspend override fun save(domains: List<ChannelDomain>, options: OrchestratorContract.Options): List<ChannelDomain> {
        throw OrchestratorContract.NotImplementedException()
    }

    override suspend fun count(filter: OrchestratorContract.Filter, options: OrchestratorContract.Options): Int {
        throw OrchestratorContract.NotImplementedException()
    }

    override suspend fun delete(domain: ChannelDomain, options: OrchestratorContract.Options): Boolean {
        throw OrchestratorContract.NotImplementedException()
    }

    override suspend fun update(update: UpdateObject<ChannelDomain>, options: OrchestratorContract.Options): ChannelDomain? {
        throw OrchestratorContract.NotImplementedException()
    }


}