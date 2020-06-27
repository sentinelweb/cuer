package uk.co.sentinelweb.cuer.app.db.repository

import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.dao.MediaDao
import uk.co.sentinelweb.cuer.app.db.dao.PlaylistDao
import uk.co.sentinelweb.cuer.app.db.dao.PlaylistItemDao
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistAndItems
import uk.co.sentinelweb.cuer.app.db.mapper.PlaylistItemMapper
import uk.co.sentinelweb.cuer.app.db.mapper.PlaylistMapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistDatabaseRepository constructor(
    private val playlistDao: PlaylistDao,
    private val playlistMapper: PlaylistMapper,
    private val playlistItemDao: PlaylistItemDao,
    private val playlistItemMapper: PlaylistItemMapper,
    private val mediaDao: MediaDao,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper
) : DatabaseRepository<PlaylistDomain> {

    override suspend fun save(domain: PlaylistDomain, flat: Boolean): RepoResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                domain
                    .let { playlistMapper.map(it) }
                    .let { playlistDao.insert(it) }
                    .takeIf { flat }
                    ?.let { id -> domain.items.map { it to id } }
                    ?.map { (item, id) -> playlistItemMapper.map(item, id) }
                    ?.let { playlistItems -> playlistItemDao.insertAll(playlistItems) }
                RepoResult.Data.Empty(true)
            } catch (e: Exception) {
                val msg = "couldn't save playlist ${domain.title}"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }

    override suspend fun save(domains: List<PlaylistDomain>, flat: Boolean): RepoResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                domains
                    .map { playlistMapper.map(it) }
                    .let { playlistDao.insertAll(it) }
                    .takeIf { flat }
                    ?.mapIndexed { index, id -> domains[index] to id }
                    ?.map { (playlist, id) ->
                        playlist.items
                            .map { playlistItemMapper.map(it, id) }
                            .let { playlistItems -> playlistItemDao.insertAll(playlistItems) }
                    }
                RepoResult.Data.Empty(true)
            } catch (e: Exception) {
                val msg = "couldn't save playlists ${domains.joinToString("'") { it.title }}"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }

    override suspend fun load(id: Long, flat: Boolean): RepoResult<PlaylistDomain> =
        withContext(coProvider.IO) {
            try {
                if (flat) {
                    playlistDao.load(id)!!
                        .let { playlistMapper.map(it, null, null) }
                } else {
                    mapDeep(playlistDao.loadWithItems(id)!!)
                }
                    .let { domain -> RepoResult.Data(domain) }
            } catch (e: Exception) {
                val msg = "couldn't load $id"
                log.e(msg, e)
                RepoResult.Error<PlaylistDomain>(e, msg)
            }
        }

    private suspend fun mapDeep(loadWithItems: PlaylistAndItems) =
        loadWithItems
            .let {
                playlistMapper.map(
                    it.playlist,
                    it.items,
                    mediaDao.loadAllByIds(it.items.map { it.mediaId }.toLongArray())
                )
            }

    override suspend fun loadList(filter: DatabaseRepository.Filter?): RepoResult<List<PlaylistDomain>> =
        withContext(coProvider.IO) {
            try {
                when (filter) {
                    is IdListFilter ->
                        if (filter.flat) playlistDao
                            .loadAllByIds(filter.ids.toLongArray())
                            .map { playlistMapper.map(it, null, null) }
                        else playlistDao
                            .loadAllByIdsWithItems(filter.ids.toLongArray())
                            .map { mapDeep(it) }

//                    is AllFilter -> // todo
                    else ->
                        playlistDao
                            .getAllPlaylists()
                            .map { playlistMapper.map(it, null, null) }
                }.let { RepoResult.Data(it) }
            } catch (e: Throwable) {
                val msg = "couldn't load $filter"
                log.e(msg, e)
                RepoResult.Error<List<PlaylistDomain>>(e, msg)
            }
        }
//        listOf("Default", "Music", "Video", "News", "Philosophy", "Psychology", "Comedy")
//            .map { PlaylistDomain(items = listOf(), title = it, id = it/* todo db key */) }
//            .let { playlists -> RepoResult.Data(playlists) }

    override suspend fun count(filter: DatabaseRepository.Filter?): RepoResult<Int> =
        try {
            withContext(coProvider.IO) {
                playlistDao.count()
            }.let { RepoResult.Data(it) }
        } catch (e: Exception) {
            val msg = "couldn't count ${filter}"
            log.e(msg, e)
            RepoResult.Error<Int>(e, msg)
        }

    override suspend fun delete(domain: PlaylistDomain): RepoResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                domain
                    .let { playlistMapper.map(it) }
                    .also { playlistDao.delete(it) }
                    .also { playlistItemDao.deletePlaylistItems(it.id) }
                RepoResult.Data.Empty(true)
            } catch (e: Exception) {
                val msg = "couldn't delete ${domain.id}"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }

    override suspend fun deleteAll(): RepoResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                playlistDao.deleteAll()
                playlistItemDao.deleteAll()
                RepoResult.Data.Empty(true)
            } catch (e: Exception) {
                val msg = "couldn't delete all media"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }

    class AllFilter(val flat: Boolean = true) : DatabaseRepository.Filter
    class IdListFilter(val ids: List<Long>, val flat: Boolean = true) : DatabaseRepository.Filter
}