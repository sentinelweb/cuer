package uk.co.sentinelweb.cuer.app.db.repository

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.AppDatabase
import uk.co.sentinelweb.cuer.app.db.dao.MediaDao
import uk.co.sentinelweb.cuer.app.db.mapper.update.MediaUpdateMapper
import uk.co.sentinelweb.cuer.app.db.mapper.MediaMapper
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult.Data
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult.Data.Empty
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.*
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.update.MediaPositionUpdateDomain
import uk.co.sentinelweb.cuer.domain.update.MediaUpdateDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain
import java.io.InvalidClassException

// todo emit
@Suppress("DEPRECATION")
class RoomMediaDatabaseRepository constructor(
    private val mediaDao: MediaDao,
    private val mediaMapper: MediaMapper,
    private val channelDatabaseRepository: RoomChannelDatabaseRepository,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper,
    private val database: AppDatabase,
    private val mediaUpdateMapper: MediaUpdateMapper,
) : MediaDatabaseRepository {

    init {
        log.tag(this)
    }

    private val _mediaFlow = MutableSharedFlow<Pair<Operation, MediaDomain>>()
    override val updates: Flow<Pair<Operation, MediaDomain>>
        get() = _mediaFlow

    override val stats: Flow<Pair<Operation, Nothing>>
        get() = TODO("Not yet implemented")

    @Transaction
    override suspend fun save(domain: MediaDomain, flat: Boolean, emit: Boolean): RepoResult<MediaDomain> =
        withContext(coProvider.IO) {
            try {
                domain
                    .let { if (!flat) it.copy(channelData = channelDatabaseRepository.checkToSaveChannel(it.channelData)) else it }
                    .let { mediaMapper.map(it) }
                    .let { mediaDao.insert(it) }
                    .let { Data(load(id = it, flat).data) }
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
                    .map { if (!flat) it.copy(channelData = channelDatabaseRepository.checkToSaveChannel(it.channelData)) else it }
                    .map { mediaMapper.map(it) }
                    .let { mediaDao.insertAll(it) }
                    .also { database.setTransactionSuccessful() }
                    .also { database.endTransaction() }
                    .let { idlist -> Data(loadList(IdListFilter(idlist), flat).data) }
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

    override suspend fun loadList(filter: Filter?, flat: Boolean)
            : RepoResult<List<MediaDomain>> = withContext(coProvider.IO) {
        try {// todo better transactions
            database.beginTransaction()
            when (filter) {
                is IdListFilter ->
                    mediaDao
                        .loadAllByIds(filter.ids.toLongArray())
                        .map { mediaMapper.map(it) }
                        .let { Data(it) }
                is PlatformIdListFilter ->
                    filter.ids
                        .mapNotNull { mediaDao.findByMediaId(it) }
                        .map { mediaMapper.map(it) }
                        .let { Data.dataOrEmpty(it) }
                is ChannelPlatformIdFilter -> TODO()
                else -> throw IllegalArgumentException("$filter not implemented")
//                else ->// todo return empty for else
//                    mediaDao
//                        .getAll()
//                        .map { mediaMapper.map(it) }
//                        .let { Data(it) }
            }.also { database.setTransactionSuccessful() }
//            }})
        } catch (e: Throwable) {
            val msg = "couldn't load $filter"
            log.e(msg, e)
            RepoResult.Error<List<MediaDomain>>(e, msg)
        }.also { database.endTransaction() }
    }

    override suspend fun loadStatsList(filter: Filter?): RepoResult<List<Nothing>> {
        TODO("Not yet implemented")
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

    override suspend fun count(filter: Filter?): RepoResult<Int> =
        try {
            withContext(coProvider.IO) {
                mediaDao.count()
            }.let { Data(it) }
        } catch (e: Throwable) {
            val msg = "couldn't count ${filter}"
            log.e(msg, e)
            RepoResult.Error<Int>(e, msg)
        }

    override suspend fun update(update: UpdateDomain<MediaDomain>, flat: Boolean, emit: Boolean): RepoResult<MediaDomain> =
        withContext(coProvider.IO) {
            try {
                when (update as? MediaUpdateDomain) {
                    is MediaPositionUpdateDomain ->
                        (update as MediaPositionUpdateDomain)
                            // .apply { database.beginTransaction() }
                            .let { it to mediaDao.getFlags(it.id) }
                            .let { mediaUpdateMapper.map(it.first, it.second) }
                            .also { mediaDao.updatePosition(it.id, it.dateLastPlayed, it.positon, it.duration, it.flags) }
//                                    .also { database.setTransactionSuccessful() }
//                                    .also { database.endTransaction() }
                            .let { Data(load(id = it.id, flat).data) }
//                            .also { log.d("media: ${it.data?.dateLastPlayed}") }
                            .also { if (emit) it.data?.also { _mediaFlow.emit((if (flat) FLAT else FULL) to it) } }
                    else -> throw InvalidClassException("update object not valid: ${update::class.simpleName}")
                }
            } catch (e: Throwable) {
                val msg = "couldn't delete all channels"
                log.e(msg, e)
//                database.endTransaction()
                RepoResult.Error<MediaDomain>(e, msg)
            }
        }

}
