package uk.co.sentinelweb.cuer.app.db.repository

import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.dao.MediaDao
import uk.co.sentinelweb.cuer.app.db.mapper.MediaMapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.MediaDomain

class MediaDatabaseRepository constructor(
    private val mediaDao: MediaDao,
    private val mediaMapper: MediaMapper,
    private val coProvider: CoroutineContextProvider
) : DatabaseRepository<MediaDomain> {

    override suspend fun save(domain: MediaDomain): Boolean = withContext(coProvider.IO) {
        domain
            .let { mediaMapper.map(it) }
            .also { mediaDao.insertAll(listOf(it)) }
        true
    }

    override suspend fun save(domains: List<MediaDomain>): Boolean = withContext(coProvider.IO) {
        domains
            .map { mediaMapper.map(it) }
            .also { mediaDao.insertAll(it) }
        true
    }

    override suspend fun load(id: Int): MediaDomain = withContext(coProvider.IO) {
        mediaDao.load(id)
            .let { mediaMapper.map(it) }
    }

    override suspend fun loadList(filter: DatabaseRepository.Filter?)
            : List<MediaDomain> = withContext(coProvider.IO) {
        when (filter) {
            is IdListFilter ->
                mediaDao
                    .loadAllByIds(filter.ids.toIntArray())
                    .map { mediaMapper.map(it) }
            is MediaIdFilter ->
                mediaDao
                    .findByMediaId(filter.mediaId)
                    .let { listOf(mediaMapper.map(it)) }
            else ->
                mediaDao
                    .getAll()
                    .map { mediaMapper.map(it) }
        }
    }

    override suspend fun delete(domain: MediaDomain) = withContext(coProvider.IO) {
        domain
            .let { mediaMapper.map(it) }
            .also { mediaDao.delete(it) }
        true
    }

    override suspend fun count(filter: DatabaseRepository.Filter?): Int  = withContext(coProvider.IO) {
        mediaDao.count()
    }

    class NoFilter() : DatabaseRepository.Filter
    class IdListFilter(val ids: List<Int>) : DatabaseRepository.Filter
    class MediaIdFilter(val mediaId: String) : DatabaseRepository.Filter


}