package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.db.repository.ChannelDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor

class ChannelOrchestrator constructor(
    private val channelDatabaseRepository: ChannelDatabaseRepository,
    private val ytInteractor: YoutubeInteractor
) : OrchestratorContract<ChannelDomain> {
    override val updates: Flow<Triple<Operation, Source, ChannelDomain>>
        get() = throw NotImplementedException()

    suspend override fun loadById(id: GUID, options: Options): ChannelDomain? {
        throw NotImplementedException()
    }

    suspend override fun loadList(filter: Filter, options: Options): List<ChannelDomain> {
        throw NotImplementedException()
    }

    suspend override fun loadByPlatformId(platformId: String, options: Options): ChannelDomain? {
        throw NotImplementedException()
    }

    suspend override fun loadByDomain(domain: ChannelDomain, options: Options): ChannelDomain? {
        throw NotImplementedException()
    }

    suspend override fun save(domain: ChannelDomain, options: Options): ChannelDomain {
        throw NotImplementedException()
    }

    suspend override fun save(domains: List<ChannelDomain>, options: Options): List<ChannelDomain> {
        throw NotImplementedException()
    }

    override suspend fun count(filter: Filter, options: Options): Int =
        when (options.source) {
            Source.MEMORY -> throw NotImplementedException()
            Source.LOCAL ->
                channelDatabaseRepository.count(filter)
                    .forceDatabaseSuccessNotNull("Count failed $filter")

            Source.LOCAL_NETWORK -> throw NotImplementedException()
            Source.REMOTE -> throw NotImplementedException()
            Source.PLATFORM -> throw InvalidOperationException(this::class, null, options)
        }

    override suspend fun delete(id: GUID, options: Options): Boolean {
        throw NotImplementedException()
    }

    override suspend fun delete(domain: ChannelDomain, options: Options): Boolean {
        throw NotImplementedException()
    }

    override suspend fun update(update: UpdateDomain<ChannelDomain>, options: Options): ChannelDomain? {
        throw NotImplementedException()
    }
}
