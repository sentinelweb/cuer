package uk.co.sentinelweb.cuer.app.db.repository

import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.AppDatabase.Companion.INITIAL_ID
import uk.co.sentinelweb.cuer.app.db.dao.MediaDao
import uk.co.sentinelweb.cuer.app.db.dao.PlaylistDao
import uk.co.sentinelweb.cuer.app.db.dao.PlaylistItemDao
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistAndItems
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistEntity
import uk.co.sentinelweb.cuer.app.db.mapper.PlaylistItemMapper
import uk.co.sentinelweb.cuer.app.db.mapper.PlaylistMapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlaylistDatabaseRepository constructor(
    private val playlistDao: PlaylistDao,
    private val playlistMapper: PlaylistMapper,
    private val playlistItemDao: PlaylistItemDao,
    private val playlistItemMapper: PlaylistItemMapper,
    private val mediaDao: MediaDao,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper
) : DatabaseRepository<PlaylistDomain> {

    override suspend fun save(domain: PlaylistDomain, flat: Boolean): RepoResult<PlaylistDomain> =
        withContext(coProvider.IO) {
            try {
                var insertId = -1L
                domain
                    .let { playlistMapper.map(it) }
                    .let { playlistDao.insert(it) }
                    .also { insertId = it }
                    .takeIf { !flat }
                    ?.let { playlistId -> domain.items.map { it.copy(playlistId = playlistId) } }
                    ?.map { item -> playlistItemMapper.map(item) }
                    ?.let { playlistItemEntities -> playlistItemDao.insertAll(playlistItemEntities) }

                RepoResult.Data(load(insertId, flat).data)
            } catch (e: Exception) {
                val msg = "couldn't save playlist ${domain.title}"
                log.e(msg, e)
                RepoResult.Error<PlaylistDomain>(e, msg)
            }
        }

    override suspend fun save(
        domains: List<PlaylistDomain>,
        flat: Boolean
    ): RepoResult<List<PlaylistDomain>> =
        withContext(coProvider.IO) {
            try {
                var insertIds = listOf<Long>()
                domains
                    .map { playlistMapper.map(it) }
                    .let { playlistDao.insertAll(it) }
                    .also { insertIds = it }
                    .takeIf { !flat }
                    ?.mapIndexed { index, playlistId -> domains[index] to playlistId }
                    ?.map { (playlist, playlistId) ->
                        playlist.items
                            .map { it.copy(playlistId = playlistId) }
                            .map { playlistItemMapper.map(it) }
                            .let { playlistItems -> playlistItemDao.insertAll(playlistItems) }
                    }

                RepoResult.Data(loadList(IdListFilter(insertIds, flat)).data)
            } catch (e: Exception) {
                val msg = "couldn't save playlists ${domains.joinToString { it.title }}"
                log.e(msg, e)
                RepoResult.Error<List<PlaylistDomain>>(e, msg)
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
                    is DefaultFilter ->
                        if (filter.flat) playlistDao
                            .loadAllByFlags(PlaylistEntity.FLAG_DEFAULT)
                            .map { playlistMapper.map(it, null, null) }
                        else playlistDao
                            .loadAllByFlagsWithItems(PlaylistEntity.FLAG_DEFAULT)
                            .map { mapDeep(it) }
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

    // region PlaylistItemDomain
    suspend fun savePlaylistItem(item: PlaylistItemDomain): RepoResult<PlaylistItemDomain> =
        withContext(coProvider.IO) {
            try {
                item
                    .let { playlistItemMapper.map(item) }
                    .let { itemEntity ->
                        if (itemEntity.id != INITIAL_ID) {
                            // check record exists
                            playlistItemDao.load(itemEntity.id)?.run {
                                playlistItemDao.update(itemEntity); itemEntity
                            } ?: itemEntity.copy(id = playlistItemDao.insert(itemEntity))
                        } else {
                            itemEntity.copy(id = playlistItemDao.insert(itemEntity))
                        }
                    }
                    .let { playlistItemMapper.map(it, item.media) }
                    .let { RepoResult.Data(it) }
            } catch (e: Exception) {
                val msg = "couldn't save playlist item"
                log.e(msg, e)
                RepoResult.Error<PlaylistItemDomain>(e, msg)
            }
        }

    suspend fun savePlaylistItems(items: List<PlaylistItemDomain>): RepoResult<List<PlaylistItemDomain>> =
        withContext(coProvider.IO) {
            try {
                val checkOrderAndPlaylist: MutableSet<String> = mutableSetOf()
                items
                    .apply {
                        forEach {
                            val key = "${it.order}:${it.playlistId}"
                            if (checkOrderAndPlaylist.contains(key)) throw IllegalStateException("order / playlist is not unique")
                            else checkOrderAndPlaylist.add(key)
                        }
                    }
                    .map { playlistItemMapper.map(it) }
                    .map {
                        if (it.id != INITIAL_ID) {
                            playlistItemDao.update(it); it
                        } else {
                            it.copy(id = playlistItemDao.insert(it))
                        }
                    }
                    .map { savedItem ->
                        playlistItemMapper.map(
                            savedItem,
                            items.find { it.media.id == savedItem.mediaId }
                                ?.media
                                ?: throw IllegalStateException("Media id saved incorrectly")
                        )
                    }
                    .let { RepoResult.Data(it) }
            } catch (e: Exception) {
                val msg = "couldn't save playlist items"
                log.e(msg, e)
                RepoResult.Error<List<PlaylistItemDomain>>(e, msg)
            }
        }

    suspend fun loadPlaylistItem(id: Long): RepoResult<PlaylistItemDomain> =
        withContext(coProvider.IO) {
            try {
                playlistItemDao.load(id)!!
                    .let { playlistItemMapper.map(it, mediaDao.load(it.mediaId)!!) }
                    .let { RepoResult.Data(it) }
            } catch (e: Exception) {
                val msg = "couldn't save playlist item"
                log.e(msg, e)
                RepoResult.Error<PlaylistItemDomain>(e, msg)
            }
        }

    suspend fun loadPlaylistItems(filter: DatabaseRepository.Filter): RepoResult<List<PlaylistItemDomain>> =
        withContext(coProvider.IO) {
            try {
                when (filter) {
                    is MediaIdListFilter -> playlistItemDao.loadItemsByMediaId(filter.ids)
                        .map { playlistItemMapper.map(it, mediaDao.load(it.mediaId)!!) }
                    else -> listOf()
                }.let { RepoResult.Data(it) }
            } catch (e: Exception) {
                val msg = "couldn't save playlist item"
                log.e(msg, e)
                RepoResult.Error<List<PlaylistItemDomain>>(e, msg)
            }
        }

    suspend fun delete(domain: PlaylistItemDomain): RepoResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                domain
                    .let { playlistItemMapper.map(it) }
                    .also { playlistItemDao.delete(it) }
                RepoResult.Data.Empty(true)
            } catch (e: Exception) {
                val msg = "couldn't delete ${domain.id}"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }
    // endregion

    class IdListFilter(val ids: List<Long>, val flat: Boolean = true) : DatabaseRepository.Filter
    class MediaIdListFilter(val ids: List<Long>, val flat: Boolean = true) : DatabaseRepository.Filter
    class DefaultFilter(val flat: Boolean = true) : DatabaseRepository.Filter
}