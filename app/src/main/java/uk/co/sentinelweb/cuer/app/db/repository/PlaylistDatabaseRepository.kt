package uk.co.sentinelweb.cuer.app.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.AppDatabase
import uk.co.sentinelweb.cuer.app.db.AppDatabase.Companion.INITIAL_ID
import uk.co.sentinelweb.cuer.app.db.dao.MediaDao
import uk.co.sentinelweb.cuer.app.db.dao.PlaylistDao
import uk.co.sentinelweb.cuer.app.db.dao.PlaylistItemDao
import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistAndItems
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistEntity
import uk.co.sentinelweb.cuer.app.db.mapper.PlaylistItemMapper
import uk.co.sentinelweb.cuer.app.db.mapper.PlaylistMapper
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.*
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain

// todo update stats automatically on save/delete
@Suppress("DEPRECATION")
class PlaylistDatabaseRepository constructor(
    private val playlistDao: PlaylistDao,
    private val playlistMapper: PlaylistMapper,
    private val playlistItemDao: PlaylistItemDao,
    private val playlistItemMapper: PlaylistItemMapper,
    private val mediaDao: MediaDao,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper,
    private val database: AppDatabase
) : DatabaseRepository<PlaylistDomain> {// todo , DatabaseRepository<PlaylistItemDomain>

    private val _playlistFlow = MutableSharedFlow<Pair<Operation, PlaylistDomain>>()
    override val updates: Flow<Pair<Operation, PlaylistDomain>>
        get() = _playlistFlow

    private val _playlistItemFlow = MutableSharedFlow<Pair<Operation, PlaylistItemDomain>>()
    val playlistItemFlow: Flow<Pair<Operation, PlaylistItemDomain>>
        get() = _playlistItemFlow

    private val _playlistStatFlow = MutableSharedFlow<Pair<Operation, PlaylistStatDomain>>()

    val playlistStatFlow: Flow<Pair<Operation, PlaylistStatDomain>>
        get() = _playlistStatFlow

    private val _playlistStats: MutableList<PlaylistStatDomain> = mutableListOf()
    private val playlistStats: List<PlaylistStatDomain> = _playlistStats

    init {
//        coProvider.computationScope.launch {
//            // todo error check
//            loadList(null).data?.map { it.id!! }
//                ?.apply {
//                    _playlistStats.addAll(loadPlaylistStatList(this).data!!)
//                }
//        }
    }

    override suspend fun save(domain: PlaylistDomain, flat: Boolean, emit: Boolean): RepoResult<PlaylistDomain> =
        withContext(coProvider.IO) {
            try {
                var insertId = -1L
                domain
                    .also { database.beginTransaction() }
                    .let { playlistMapper.map(it) }
                    .let { playlistDao.insert(it) }/* todo channels */
                    .also { insertId = it }
                    .takeIf { !flat }
                    ?.let { playlistId -> domain.items.map { it.copy(playlistId = playlistId) } }
                    ?.map { item -> playlistItemMapper.map(item) }
                    ?.let { playlistItemEntities -> playlistItemDao.insertAll(playlistItemEntities) }
                    .also { database.setTransactionSuccessful() }
                    .also { database.endTransaction() }
                load(insertId, flat)
                    .takeIf { it.isSuccessful }
                    ?.also { if (emit) it.data?.apply { _playlistFlow.emit((if (flat) FLAT else FULL) to this) } }
                    ?: throw IllegalStateException("Couldn't load saved data")
            } catch (e: Throwable) {
                val msg = "couldn't save playlist ${domain.title}"
                log.e(msg, e)
                database.endTransaction()
                RepoResult.Error<PlaylistDomain>(e, msg)
            }
        }

    override suspend fun save(domains: List<PlaylistDomain>, flat: Boolean, emit: Boolean): RepoResult<List<PlaylistDomain>> =
        withContext(coProvider.IO) {
            try {
                var insertIds = listOf<Long>()
                domains
                    .also { database.beginTransaction() }
                    .map { playlistMapper.map(it) }
                    .let { playlistDao.insertAll(it) }/* todo channels */
                    .also { insertIds = it }
                    .takeIf { !flat }
                    ?.mapIndexed { index, playlistId -> domains[index] to playlistId }
                    ?.map { (playlist, playlistId) ->
                        playlist.items
                            .map { it.copy(playlistId = playlistId) }
                            .map { playlistItemMapper.map(it) }
                            .let { playlistItems -> playlistItemDao.insertAll(playlistItems) }
                    }
                    .also { database.setTransactionSuccessful() }
                    .also { database.endTransaction() }

                RepoResult.Data(loadList(IdListFilter(insertIds), flat).data)
                    .also { if (emit) it.data?.forEach { _playlistFlow.emit((if (flat) FLAT else FULL) to it) } }
            } catch (e: Throwable) {
                val msg = "couldn't save playlists ${domains.joinToString { it.title }}"
                log.e(msg, e)
                database.endTransaction()
                RepoResult.Error<List<PlaylistDomain>>(e, msg)
            }
        }

    override suspend fun load(id: Long, flat: Boolean): RepoResult<PlaylistDomain> =
        withContext(coProvider.IO) {
            try {
                database.beginTransaction()
                if (flat) {
                    playlistDao.load(id)!!
                        .let { playlistMapper.map(it, null, null, null) }/* todo channels */
                } else {
                    mapDeep(playlistDao.loadWithItems(id)!!)
                }
                    .let { domain -> RepoResult.Data(domain) }
                    .also { database.setTransactionSuccessful() }
            } catch (e: Exception) {
                val msg = "couldn't load $id"
                log.e(msg, e)
                RepoResult.Error<PlaylistDomain>(e, msg)
            }.also { database.endTransaction() }
        }

    private suspend fun mapDeep(loadWithItems: PlaylistAndItems): PlaylistDomain =
        loadWithItems
            .let {
                playlistMapper.map(
                    it.playlist,
                    it.items,
                    mediaDao.loadAllByIds(it.items.map { it.mediaId }.toLongArray()),
                    null/* todo channels */
                )
            }

    override suspend fun loadList(filter: Filter?, flat: Boolean): RepoResult<List<PlaylistDomain>> =
        withContext(coProvider.IO) {
            try {
                playlistDao
                    .also { database.beginTransaction() }
                    .let { playlistDao ->
                        when (filter) {
                            is IdListFilter ->
                                if (flat)
                                    playlistDao.loadAllByIds(filter.ids.toLongArray())
                                        .map { playlistMapper.map(it, null, null, null) }
                                else playlistDao
                                    .loadAllByIdsWithItems(filter.ids.toLongArray())
                                    .map { mapDeep(it) }/* todo channels */
                            is DefaultFilter ->
                                if (flat) playlistDao
                                    .loadAllByFlags(PlaylistEntity.FLAG_DEFAULT)
                                    .map { playlistMapper.map(it, null, null, null) }
                                else playlistDao
                                    .loadAllByFlagsWithItems(PlaylistEntity.FLAG_DEFAULT)
                                    .map { mapDeep(it) }/* todo channels */
                            is AllFilter ->
                                if (flat) playlistDao
                                    .getAllPlaylists()
                                    .map { playlistMapper.map(it, null, null, null) }
                                else playlistDao
                                    .getAllPlaylistsWithItems()
                                    .map { mapDeep(it) }/* todo channels */
                            is PlatformIdListFilter ->
                                if (flat) playlistDao
                                    .loadAllByPlatformIds(filter.ids)
                                    .map { playlistMapper.map(it, null, null, null) }
                                else playlistDao
                                    .loadAllByPlatformIdsWithItems(filter.ids)
                                    .map { mapDeep(it) }/* todo channels */
                            is ChannelPlatformIdFilter ->
                                if (flat) playlistDao
                                    .findPlaylistsForChannePlatformlId(filter.platformId)
                                    .map { playlistMapper.map(it, null, null, null) }
                                else throw IllegalArgumentException("Only flat supported for ChannelPlatformIdFilter")
                            else ->// todo return empty for else
                                playlistDao
                                    .getAllPlaylists()
                                    .map { playlistMapper.map(it, null, null, null) }
                        }
                    }.let { RepoResult.Data(it) }
                    .also { database.setTransactionSuccessful() }
            } catch (e: Throwable) {
                val msg = "couldn't load $filter"
                log.e(msg, e)
                RepoResult.Error<List<PlaylistDomain>>(e, msg)
            }.also { database.endTransaction() }
        }

    override suspend fun count(filter: Filter?): RepoResult<Int> =
        try {
            withContext(coProvider.IO) {
                playlistDao.count()
            }.let { RepoResult.Data(it) }
        } catch (e: Exception) {
            val msg = "couldn't count ${filter}"
            log.e(msg, e)
            RepoResult.Error<Int>(e, msg)
        }

    override suspend fun delete(domain: PlaylistDomain, emit: Boolean): RepoResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                domain
                    .also { database.beginTransaction() }
                    .let { playlistMapper.map(it) }
                    .also { playlistDao.delete(it) }
                    .also { playlistItemDao.deletePlaylistItems(it.id) }
                    .also { database.setTransactionSuccessful() }
                    .also { database.endTransaction() }
                    .also {
                        if (emit) {
                            _playlistFlow.emit(DELETE to domain)
                        }
                    }
                RepoResult.Data.Empty(true)
            } catch (e: Throwable) {
                val msg = "couldn't delete ${domain.id}"
                log.e(msg, e)
                database.endTransaction()
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

    suspend fun updateCurrentIndex(playlist: PlaylistDomain, emit: Boolean = true): RepoResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                RepoResult.Data(playlist.id?.let {
                    playlistDao.updateIndex(it, playlist.currentIndex) > 0
                } ?: false).also {
                    if (emit) {
                        _playlistFlow.emit(FLAT to playlist)
                    }
                }

            } catch (e: Exception) {
                val msg = "couldn't delete all media"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }

    // region PlaylistStatDomain
    suspend fun loadPlaylistStatList(playlistIds: List<Long>, emit: Boolean = false): RepoResult<List<PlaylistStatDomain>> =
        withContext(coProvider.IO) {
            try {
                database.beginTransaction()
                RepoResult.Data(
                    playlistIds.map {
                        PlaylistStatDomain(
                            playlistId = it,
                            itemCount = playlistItemDao.countItems(it),
                            watchedItemCount = playlistItemDao.countMediaFlags(it, MediaEntity.FLAG_WATCHED)
                        )
                    }).also { database.setTransactionSuccessful() }
                    .also { if (emit) it.data?.forEach { _playlistStatFlow.emit(FLAT to it) } }
            } catch (e: Throwable) {
                val msg = "couldn't delete all media"
                log.e(msg, e)
                RepoResult.Error<List<PlaylistStatDomain>>(e, msg)
            }.also { database.endTransaction() }
        }
    // endregion PlaylistStatDomain

    // region PlaylistItemDomain
    suspend fun savePlaylistItem(item: PlaylistItemDomain, emit: Boolean = true): RepoResult<PlaylistItemDomain> =
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
                    .also { if (emit) it.data?.also { _playlistItemFlow.emit(FLAT to it) } }
            } catch (e: Throwable) {
                val msg = "couldn't save playlist item"
                log.e(msg, e)
                RepoResult.Error<PlaylistItemDomain>(e, msg)
            }
        }

    suspend fun savePlaylistItems(items: List<PlaylistItemDomain>, emit: Boolean = true): RepoResult<List<PlaylistItemDomain>> =
        withContext(coProvider.IO) {
            try {
                val checkOrderAndPlaylist: MutableSet<String> = mutableSetOf()
                items
                    .apply {
                        forEach {
                            val key = "${it.order}:${it.playlistId}"
                            if (checkOrderAndPlaylist.contains(key)) throw IllegalStateException("Order / playlist is not unique")
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
                    .also { if (emit) it.data?.forEach { _playlistItemFlow.emit(FLAT to it) } }
            } catch (e: Throwable) {
                val msg = "Couldn't save playlist items"
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
            } catch (e: Throwable) {
                val msg = "couldn't save playlist item"
                log.e(msg, e)
                RepoResult.Error<PlaylistItemDomain>(e, msg)
            }
        }

    suspend fun loadPlaylistItems(filter: Filter? = null): RepoResult<List<PlaylistItemDomain>> =
        withContext(coProvider.IO) {
            try {
                when (filter) {
                    is IdListFilter -> playlistItemDao.loadAllByIds(filter.ids)
                        .map { playlistItemMapper.map(it, mediaDao.load(it.mediaId)!!) }
                    is MediaIdListFilter -> playlistItemDao.loadItemsByMediaId(filter.ids)
                        .map { playlistItemMapper.map(it, mediaDao.load(it.mediaId)!!) }
                    else -> playlistItemDao.loadAllItems()
                        .map { playlistItemMapper.map(it, mediaDao.load(it.mediaId)!!) }
                }.let { RepoResult.Data(it) }
            } catch (e: Throwable) {
                val msg = "couldn't save playlist item"
                log.e(msg, e)
                RepoResult.Error<List<PlaylistItemDomain>>(e, msg)
            }
        }

    suspend fun delete(domain: PlaylistItemDomain, emit: Boolean = true): RepoResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                domain
                    .let { playlistItemMapper.map(it) }
                    .also { playlistItemDao.delete(it) }
                    .also {
                        if (emit) {
                            _playlistItemFlow.emit(DELETE to domain)
                        }
                    }
                RepoResult.Data.Empty(true)
            } catch (e: Throwable) {
                val msg = "couldn't delete ${domain.id}"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }
    // endregion

    suspend fun getPlaylistOrDefault(playlistId: Long?, flat: Boolean = false) =
        (playlistId
            ?.let { load(it, flat = false) }
            ?.takeIf { it.isSuccessful }
            ?.data
            ?: run {
                loadList(DefaultFilter(), flat)
                    .takeIf { it.isSuccessful && it.data?.size ?: 0 > 0 }
                    ?.data?.get(0)
            })


}