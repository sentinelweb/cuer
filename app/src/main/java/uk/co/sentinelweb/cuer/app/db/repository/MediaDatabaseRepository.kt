package uk.co.sentinelweb.cuer.app.db.repository

import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.dao.MediaDao
import uk.co.sentinelweb.cuer.app.db.mapper.MediaMapper
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult.Data
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult.Data.Empty
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain

class MediaDatabaseRepository constructor(
    private val mediaDao: MediaDao,
    private val mediaMapper: MediaMapper,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper
) : DatabaseRepository<MediaDomain> {

    init {
        log.tag = "MediaDatabaseRepository"
    }

    override suspend fun save(domain: MediaDomain, flat: Boolean): RepoResult<MediaDomain> =
        withContext(coProvider.IO) {
            try {
                domain
                    .let { mediaMapper.map(it) }
                    .let { mediaDao.insert(it) }
                    .let { Data(domain.copy(id = it.toString())) }
            } catch (e: Exception) {
                val msg = "couldn't save ${domain.url}"
                log.e(msg, e)
                RepoResult.Error<MediaDomain>(e, msg)
            }
        }

    override suspend fun save(
        domains: List<MediaDomain>,
        flat: Boolean
    ): RepoResult<List<MediaDomain>> =
        withContext(coProvider.IO) {
            try {
                domains
                    .map { mediaMapper.map(it) }
                    .let { mediaDao.insertAll(it) }
                    .let { idlist -> Data(domains.mapIndexed { i, m -> m.copy(id = idlist[i].toString()) }) }
            } catch (e: Exception) {
                val msg = "couldn't save ${domains.map { it.url }}"
                log.e(msg, e)
                RepoResult.Error<List<MediaDomain>>(e, msg)
            }
        }

    override suspend fun load(id: Long, flat: Boolean): RepoResult<MediaDomain> =
        withContext(coProvider.IO) {
            try {
                mediaDao.load(id)!!
                    .let { mediaMapper.map(it) }
                    .let { Data(it) }
            } catch (e: Exception) {
                val msg = "couldn't load $id"
                log.e(msg, e)
                RepoResult.Error<MediaDomain>(e, msg)
            }
        }

    override suspend fun loadList(filter: DatabaseRepository.Filter?)
            : RepoResult<List<MediaDomain>> = withContext(coProvider.IO) {
        try {
            when (filter) {
                is PlaylistDatabaseRepository.IdListFilter ->
                    mediaDao
                        .loadAllByIds(filter.ids.toLongArray())
                        .map { mediaMapper.map(it) }
                        .let { Data(it) }
                is MediaIdFilter ->
                    mediaDao
                        .findByMediaId(filter.mediaId)
                        ?.let { listOf(mediaMapper.map(it)) }
                        ?.let { Data.dataOrEmpty(it) }
                        ?: Empty(listOf())
                else ->
                    mediaDao
                        .getAll()
                        .map { mediaMapper.map(it) }
                        .let { Data(it) }
            }
        } catch (e: Throwable) {
            val msg = "couldn't load $filter"
            log.e(msg, e)
            RepoResult.Error<List<MediaDomain>>(e, msg)
        }
    }

    override suspend fun delete(domain: MediaDomain): RepoResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                domain
                    .let { mediaMapper.map(it) }
                    .also { mediaDao.delete(it) }
                Empty(true)
            } catch (e: Exception) {
                val msg = "couldn't delete ${domain.id}"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }

    override suspend fun deleteAll(): RepoResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                mediaDao.deleteAll()
                Empty(true)
            } catch (e: Exception) {
                val msg = "couldn't delete all media"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }

    override suspend fun count(filter: DatabaseRepository.Filter?): RepoResult<Int> =
        try {
            withContext(coProvider.IO) {
                mediaDao.count()
            }.let { Data(it) }
        } catch (e: Exception) {
            val msg = "couldn't count ${filter}"
            log.e(msg, e)
            RepoResult.Error<Int>(e, msg)
        }

    class IdListFilter(val ids: List<Long>) : DatabaseRepository.Filter
    class MediaIdFilter(val mediaId: String) : DatabaseRepository.Filter

}