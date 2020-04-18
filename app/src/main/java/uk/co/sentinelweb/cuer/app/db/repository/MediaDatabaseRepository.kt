package uk.co.sentinelweb.cuer.app.db.repository

import uk.co.sentinelweb.cuer.app.db.dao.MediaDao
import uk.co.sentinelweb.cuer.app.db.mapper.MediaMapper
import uk.co.sentinelweb.cuer.domain.MediaDomain

class MediaDatabaseRepository constructor(
    private val mediaDao: MediaDao,
    private val mediaMapper: MediaMapper
) : DatabaseRepository<MediaDomain> {

    override suspend fun save(domain: MediaDomain) {
        domain
            .let { mediaMapper.map(it) }
            .also { mediaDao.insertAll(listOf(it)) }
    }

    override suspend fun save(domains: List<MediaDomain>) {
        domains
            .map { mediaMapper.map(it) }
            .also { mediaDao.insertAll(it) }
    }

    override suspend fun load(id: Int): MediaDomain =
        mediaDao.load(id)
            .let { mediaMapper.map(it) }


    override suspend fun loadList(filter: DatabaseRepository.Filter): List<MediaDomain> {
        return when (filter) {
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

    override suspend fun delete(domain: MediaDomain) {
        domain
            .let { mediaMapper.map(it) }
            .also { mediaDao.delete(it) }
    }

    class NoFilter() : DatabaseRepository.Filter
    class IdListFilter(val ids: List<Int>) : DatabaseRepository.Filter
    class MediaIdFilter(val mediaId: String) : DatabaseRepository.Filter
}