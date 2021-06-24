package uk.co.sentinelweb.cuer.app.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.AppDatabase
import uk.co.sentinelweb.cuer.app.db.dao.ChannelDao
import uk.co.sentinelweb.cuer.app.db.mapper.ChannelMapper
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain
import java.io.InvalidObjectException

class RoomChannelDatabaseRepository constructor(
    private val channelDao: ChannelDao,
    private val channelMapper: ChannelMapper,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper,
    private val database: AppDatabase,
) : ChannelDatabaseRepository {

    override val updates: Flow<Pair<OrchestratorContract.Operation, ChannelDomain>>
        get() = TODO("Not yet implemented")

    override suspend fun save(domain: ChannelDomain, flat: Boolean, emit: Boolean): RepoResult<ChannelDomain> {
        TODO("Not yet implemented")
    }

    override suspend fun save(domains: List<ChannelDomain>, flat: Boolean, emit: Boolean): RepoResult<List<ChannelDomain>> {
        TODO("Not yet implemented")
    }

    override suspend fun load(id: Long, flat: Boolean): RepoResult<ChannelDomain> = loadChannel(id)

    override suspend fun loadList(filter: OrchestratorContract.Filter?, flat: Boolean): RepoResult<List<ChannelDomain>> {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: OrchestratorContract.Filter?): RepoResult<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(domain: ChannelDomain, emit: Boolean): RepoResult<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun update(update: UpdateDomain<ChannelDomain>, flat: Boolean, emit: Boolean): RepoResult<ChannelDomain> {
        TODO("Not yet implemented")
    }

    suspend fun loadChannel(id: Long): RepoResult<ChannelDomain> =
        withContext(coProvider.IO) {
            try {
                channelDao.load(id)!!
                    .let { channelMapper.map(it) }
                    .let { RepoResult.Data(it) }
            } catch (e: Throwable) {
                val msg = "couldn't load $id"
                log.e(msg, e)
                RepoResult.Error<ChannelDomain>(e, msg)
            }
        }

    suspend fun checkToSaveChannel(channel: ChannelDomain): ChannelDomain {
        if (channel.platformId.isNullOrEmpty())
            throw InvalidObjectException("Channel data is missing remoteID")
        return channel
            .let { channelMapper.map(it) }
            .let { toCheck ->
                channelDao.findByChannelId(toCheck.remoteId)?.let { saved ->
                    // check for updated channel data + save
                    if (toCheck.image != saved.image ||
                        toCheck.thumbNail != saved.thumbNail ||
                        toCheck.published != saved.published ||
                        toCheck.description != saved.description
                    ) {
                        channelDao.update(toCheck.copy(id = saved.id))
                    }
                    saved.id
                } ?: channelDao.insert(toCheck)
            }
            .let { channel.copy(id = it) }
    }

    override suspend fun deleteAll(): RepoResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                channelDao
                    .also { database.beginTransaction() }
                    .deleteAll()
                    .also { database.setTransactionSuccessful() }
                RepoResult.Data.Empty(true)
            } catch (e: Throwable) {
                val msg = "couldn't delete all channels"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }.also { database.endTransaction() }

}