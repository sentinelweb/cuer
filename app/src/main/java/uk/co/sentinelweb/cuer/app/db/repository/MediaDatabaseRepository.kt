package uk.co.sentinelweb.cuer.app.db.repository

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.AppDatabase
import uk.co.sentinelweb.cuer.app.db.dao.ChannelDao
import uk.co.sentinelweb.cuer.app.db.dao.MediaDao
import uk.co.sentinelweb.cuer.app.db.mapper.ChannelMapper
import uk.co.sentinelweb.cuer.app.db.mapper.MediaMapper
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult.Data
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult.Data.Empty
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.*
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import java.io.InvalidObjectException

// todo emit
@Suppress("DEPRECATION")
class MediaDatabaseRepository constructor(
    private val mediaDao: MediaDao,
    private val mediaMapper: MediaMapper,
    private val channelDao: ChannelDao,
    private val channelMapper: ChannelMapper,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper,
    private val database: AppDatabase
) : DatabaseRepository<MediaDomain> {// todo extract channel repo

    init {
        log.tag(this)
    }

    private val _mediaFlow = MutableSharedFlow<Pair<OrchestratorContract.Operation, MediaDomain>>()
    override val updates: Flow<Pair<OrchestratorContract.Operation, MediaDomain>>
        get() = _mediaFlow

    @Transaction
    override suspend fun save(domain: MediaDomain, flat: Boolean, emit: Boolean): RepoResult<MediaDomain> =
        withContext(coProvider.IO) {
            try {
                domain
                    .let { if (!flat) checkToSaveChannel(it) else it }
                    .let { mediaMapper.map(it) }
                    .let { mediaDao.insert(it) }
                    .let { Data(load(id = it).data) }
                    .also { if (emit) it.data?.also { _mediaFlow.emit((if (flat) FLAT else FULL) to it) } }
            } catch (e: Exception) {
                val msg = "couldn't save ${domain.url}"
                log.e(msg, e)
                RepoResult.Error<MediaDomain>(e, msg)
            }
        }

    override suspend fun save(domains: List<MediaDomain>, flat: Boolean, emit: Boolean)
            : RepoResult<List<MediaDomain>> =
        withContext(coProvider.IO) {
            try { // todo better transactions
                domains
                    .also { database.beginTransaction() }
                    .map { if (!flat) checkToSaveChannel(it) else it }
                    .map { mediaMapper.map(it) }
                    .let { mediaDao.insertAll(it) }
                    .also { database.setTransactionSuccessful() }
                    .also { database.endTransaction() }
                    .let { idlist -> Data(loadList(OrchestratorContract.IdListFilter(idlist)).data) }
                    .also { if (emit) it.data?.forEach { _mediaFlow.emit((if (flat) FLAT else FULL) to it) } }
            } catch (e: Throwable) {
                val msg = "Couldn't save ${domains.map { it.url }}"
                log.e(msg, e)
                database.endTransaction()
                RepoResult.Error<List<MediaDomain>>(e, msg)
            }

        }

    override suspend fun load(id: Long, flat: Boolean): RepoResult<MediaDomain> =
        withContext(coProvider.IO) {
            try {
                mediaDao.load(id)!!
                    .let { mediaMapper.map(it) }
                    .let { Data(it) }
            } catch (e: Throwable) {
                val msg = "couldn't load $id"
                log.e(msg, e)
                RepoResult.Error<MediaDomain>(e, msg)
            }
        }

    override suspend fun loadList(filter: OrchestratorContract.Filter?, flat: Boolean)
            : RepoResult<List<MediaDomain>> = withContext(coProvider.IO) {
        try {// todo better transactions
            database.beginTransaction()
            when (filter) {
                is OrchestratorContract.IdListFilter ->
                    mediaDao
                        .loadAllByIds(filter.ids.toLongArray())
                        .map { mediaMapper.map(it) }
                        .let { Data(it) }
                is OrchestratorContract.PlatformIdListFilter ->
                    mediaDao
                        .findByMediaId(filter.ids[0])
                        ?.let { listOf(mediaMapper.map(it)) }
                        ?.let { Data.dataOrEmpty(it) }
                        ?: Empty(listOf())
                else ->// todo return empty for else
                    mediaDao
                        .getAll()
                        .map { mediaMapper.map(it) }
                        .let { Data(it) }
            }.also { database.setTransactionSuccessful() }
//            }})
        } catch (e: Throwable) {
            val msg = "couldn't load $filter"
            log.e(msg, e)
            RepoResult.Error<List<MediaDomain>>(e, msg)
        }.also { database.endTransaction() }
    }

    override suspend fun delete(domain: MediaDomain, emit: Boolean): RepoResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                domain
                    .let { mediaMapper.map(it) }
                    .also { mediaDao.delete(it) }
                    .also { if (emit) _mediaFlow.emit(DELETE to domain) }
                Empty(true)
            } catch (e: Throwable) {
                val msg = "couldn't delete ${domain.id}"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }

    override suspend fun deleteAll(): RepoResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                mediaDao
                    .also { database.beginTransaction() }
                    .deleteAll()
                    .also { database.setTransactionSuccessful() }
                Empty(true)
            } catch (e: Exception) {
                val msg = "couldn't delete all media"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }.also { database.endTransaction() }

    override suspend fun count(filter: OrchestratorContract.Filter?): RepoResult<Int> =
        try {
            withContext(coProvider.IO) {
                mediaDao.count()
            }.let { Data(it) }
        } catch (e: Throwable) {
            val msg = "couldn't count ${filter}"
            log.e(msg, e)
            RepoResult.Error<Int>(e, msg)
        }

    suspend fun loadChannel(id: Long): RepoResult<ChannelDomain> =
        withContext(coProvider.IO) {
            try {
                channelDao.load(id)!!
                    .let { channelMapper.map(it) }
                    .let { Data(it) }
            } catch (e: Throwable) {
                val msg = "couldn't load $id"
                log.e(msg, e)
                RepoResult.Error<ChannelDomain>(e, msg)
            }
        }

    private suspend fun checkToSaveChannel(media: MediaDomain): MediaDomain {
        if (media.channelData.platformId.isNullOrEmpty())
            throw InvalidObjectException("Channel data is missing remoteID")
        return media.channelData
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
            .let { media.copy(channelData = media.channelData.copy(id = it)) }
    }

    suspend fun deleteAllChannels(): RepoResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                channelDao
                    .also { database.beginTransaction() }
                    .deleteAll()
                    .also { database.setTransactionSuccessful() }
                Empty(true)
            } catch (e: Throwable) {
                val msg = "couldn't delete all channels"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }.also { database.endTransaction() }


}
