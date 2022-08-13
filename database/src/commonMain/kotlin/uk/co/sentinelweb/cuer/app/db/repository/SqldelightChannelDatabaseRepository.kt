package uk.co.sentinelweb.cuer.app.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.Channel
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.db.mapper.ChannelMapper
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class SqldelightChannelDatabaseRepository(
    private val database: Database,
    private val imageDatabaseRepository: SqldelightImageDatabaseRepository,
    private val channelMapper: ChannelMapper,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper,
) : ChannelDatabaseRepository {

    init {
        log.tag(this)
    }

    val _updatesFlow = MutableSharedFlow<Pair<Operation, ChannelDomain>>()
    override val updates: Flow<Pair<Operation, ChannelDomain>>
        get() = _updatesFlow

    override val stats: Flow<Pair<Operation, Nothing>>
        get() = TODO("Not yet implemented")

    override suspend fun save(domain: ChannelDomain, flat: Boolean, emit: Boolean): RepoResult<ChannelDomain> =
        withContext(coProvider.IO) {
            saveInternal(domain)
                .also {
                    it.takeIf { it.isSuccessful && emit }
                        ?.data
                        ?.also { _updatesFlow.emit(emitOperation(flat, it)) }
                }
        }

    private fun saveInternal(domain: ChannelDomain): RepoResult<ChannelDomain> =
        database.channelEntityQueries.transactionWithResult<RepoResult<ChannelDomain>> {
            try {
                domain
                    .also { it.thumbNail?.also { imageDatabaseRepository.checkToSaveImage(it) } }
                    .also { it.image?.also { imageDatabaseRepository.checkToSaveImage(it) } }
                    .let { channelMapper.map(it) }
                    .let {
                        if (it.id > 0) {
                            database.channelEntityQueries
                                .update(it)
                            domain.id!!
                        } else {
                            database.channelEntityQueries
                                .create(it)
                            database.channelEntityQueries
                                .getInsertId()
                                .executeAsOne()
                        }
                    }
                    .let { domain.copy(id = it) }
                    .let { channel: ChannelDomain -> RepoResult.Data(channel) }
            } catch (e: Throwable) {
                val msg = "couldn't save channel $domain"
                log.e(msg, e)
                rollback(RepoResult.Error<ChannelDomain>(e, msg))
            }
        }


    override suspend fun save(
        domains: List<ChannelDomain>,
        flat: Boolean,
        emit: Boolean
    ): RepoResult<List<ChannelDomain>> = withContext(coProvider.IO) {
        database.channelEntityQueries.transactionWithResult<RepoResult<List<ChannelDomain>>> {
            try {
                domains
                    .map { saveInternal(it).data!! }
                    .let { RepoResult.Data(it) }
            } catch (e: Exception) {
                val msg = "couldn't save channels"
                log.e(msg, e)
                rollback(RepoResult.Error<List<ChannelDomain>>(e, msg))
            }
        }.also {
            it.takeIf { it.isSuccessful && emit }
                ?.data
                ?.forEach { _updatesFlow.emit(emitOperation(flat, it)) }
        }
    }

    override suspend fun load(id: Long, flat: Boolean): RepoResult<ChannelDomain> = loadChannel(id)

    override suspend fun loadList(
        filter: OrchestratorContract.Filter?,
        flat: Boolean
    ): RepoResult<List<ChannelDomain>> {
        TODO("Not yet implemented")
    }

    override suspend fun loadStatsList(filter: OrchestratorContract.Filter?): RepoResult<List<Nothing>> {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: OrchestratorContract.Filter?): RepoResult<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(domain: ChannelDomain, emit: Boolean): RepoResult<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun update(
        update: UpdateDomain<ChannelDomain>,
        flat: Boolean,
        emit: Boolean
    ): RepoResult<ChannelDomain> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll(): RepoResult<Boolean> = withContext(coProvider.IO) {
        var result: RepoResult<Boolean> = RepoResult.Data(false)
        database.channelEntityQueries.transaction {
            result = try {
                database.channelEntityQueries
                    .deleteAll()
                RepoResult.Data(true)
            } catch (e: Throwable) {
                val msg = "couldn't deleteAll channels"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }
        result
    }

    internal suspend fun loadChannel(id: Long): RepoResult<ChannelDomain> =
        withContext(coProvider.IO) {
            try {
                database.channelEntityQueries
                    .load(id)
                    .executeAsOneOrNull()!!
                    .let { channel: Channel ->
                        channelMapper.map(
                            channel,
                            imageDatabaseRepository.loadEntity(channel.thumb_id),
                            imageDatabaseRepository.loadEntity(channel.image_id),
                        )
                    }
                    .let { channel: ChannelDomain -> RepoResult.Data(channel) }
            } catch (e: Throwable) {
                val msg = "couldn't load $id"
                log.e(msg, e)
                RepoResult.Error<ChannelDomain>(e, msg)
            }
        }

    internal suspend fun checkToSaveChannel(domain: ChannelDomain): ChannelDomain = withContext(coProvider.IO) {
        if (domain.platformId.isNullOrEmpty())
            throw IllegalArgumentException("Channel data is missing remoteID")
        database.channelEntityQueries.transactionWithResult {
            domain
                .let { channelMapper.map(it) }
                .let { toCheck ->
                    database.channelEntityQueries
                        .findByPlatformId(toCheck.platform_id, toCheck.platform)
                        .executeAsOneOrNull()
                        ?.let { saved ->
                            // check for updated channel data + save
                            if (toCheck.image_id != saved.image_id ||
                                toCheck.thumb_id != saved.thumb_id ||
                                toCheck.published != saved.published ||
                                toCheck.description != saved.description
                            ) {
                                database.channelEntityQueries.update(toCheck.copy(id = saved.id))
                            }
                            saved.id
                        }
                        ?: let {
                            database.channelEntityQueries
                                .create(toCheck)
                            database.channelEntityQueries
                                .getInsertId()
                                .executeAsOne()
                        }
                }
                .let { domain.copy(id = it) }
                .also { it.thumbNail?.also { imageDatabaseRepository.checkToSaveImage(it) } }
                .also { it.image?.also { imageDatabaseRepository.checkToSaveImage(it) } }
        }
    }

    private fun emitOperation(
        flat: Boolean,
        it: ChannelDomain
    ) = (if (flat) Operation.FLAT else Operation.FULL) to it
}