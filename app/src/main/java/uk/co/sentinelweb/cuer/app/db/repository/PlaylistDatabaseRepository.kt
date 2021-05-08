package uk.co.sentinelweb.cuer.app.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.AppDatabase
import uk.co.sentinelweb.cuer.app.db.AppDatabase.Companion.INITIAL_ID
import uk.co.sentinelweb.cuer.app.db.dao.ChannelDao
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
import uk.co.sentinelweb.cuer.domain.update.UpdateObject

// todo update stats automatically on save/delete
@Suppress("DEPRECATION")
class PlaylistDatabaseRepository constructor(
    private val playlistDao: PlaylistDao,
    private val playlistMapper: PlaylistMapper,
    private val playlistItemDao: PlaylistItemDao,
    private val playlistItemMapper: PlaylistItemMapper,
    private val channelDao: ChannelDao,
    private val mediaRepository: MediaDatabaseRepository,
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

    //fixme: doesnt save media consider moving uk/co/sentinelweb/cuer/app/orchestrator/util/PlaylistMediaLookupOrchestrator.buildMediaLookup
    // to this class so media is auto saved
    override suspend fun save(domain: PlaylistDomain, flat: Boolean, emit: Boolean): RepoResult<PlaylistDomain> =
        withContext(coProvider.IO) {
            try {
                var insertId = -1L
                domain
                    .also { database.beginTransaction() }
                    .let { p ->
                        p.channelData?.let {
                            p.copy(channelData = mediaRepository.checkToSaveChannel(it))
                        } ?: p
                    }
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

    //fixme: doesnt save media consider moving uk/co/sentinelweb/cuer/app/orchestrator/util/PlaylistMediaLookupOrchestrator.buildMediaLookup
    // to this class so media is auto saved
    override suspend fun save(domains: List<PlaylistDomain>, flat: Boolean, emit: Boolean): RepoResult<List<PlaylistDomain>> =
        withContext(coProvider.IO) {
            try {
                var insertIds = listOf<Long>()
                domains
                    .also { database.beginTransaction() }
                    .map { p ->
                        p.channelData?.let {
                            p.copy(channelData = mediaRepository.checkToSaveChannel(it))
                        } ?: p
                    }
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
                    playlistDao.load(id)?.mapFlat() ?: throw Exception("ID not found")
                } else {
                    playlistDao.loadWithItems(id)?.mapDeep() ?: throw Exception("ID not found")
                }
                    .let { domain -> RepoResult.Data(domain) }
                    .also { database.setTransactionSuccessful() }
            } catch (e: Exception) {
                val msg = "couldn't load $id"
                log.e(msg, e)
                RepoResult.Error<PlaylistDomain>(e, msg)
            }.also { database.endTransaction() }
        }

    private suspend fun PlaylistAndItems.mapDeep(): PlaylistDomain =
        this
            .let {
                val medias = mediaRepository.loadList(IdListFilter(it.items.map { it.mediaId }), flat = false)
                    .data!!
                    .associateBy { it.id!! }
                playlistMapper.mapWithMediaDomains(
                    it.playlist,
                    it.items,
                    medias,
                    it.playlist.channelId?.let { channelDao.load(it) }
                )
            }

    private suspend fun List<PlaylistAndItems>.mapDeep() = this.map { it.mapDeep() }

    private suspend fun PlaylistEntity.mapFlat() = this.let {
        playlistMapper.mapWithMediaEntities(
            it, null, null, it.channelId?.let { channelDao.load(it) })
    }

    private suspend fun List<PlaylistEntity>.mapFlat() = this.map { it.mapFlat() }

    override suspend fun loadList(filter: Filter?, flat: Boolean): RepoResult<List<PlaylistDomain>> =
        withContext(coProvider.IO) {
            try {
                playlistDao
                    .also { database.beginTransaction() }
                    .let { playlistDao ->
                        when (filter) {
                            is IdListFilter ->
                                if (flat)
                                    playlistDao.loadAllByIds(filter.ids.toLongArray()).mapFlat()
                                else playlistDao
                                    .loadAllByIdsWithItems(filter.ids.toLongArray())
                                    .mapDeep()
                            is DefaultFilter ->
                                if (flat) playlistDao
                                    .loadAllByFlags(PlaylistEntity.FLAG_DEFAULT)
                                    .mapFlat()
                                else playlistDao
                                    .loadAllByFlagsWithItems(PlaylistEntity.FLAG_DEFAULT)
                                    .mapDeep()
                            is AllFilter ->
                                if (flat) playlistDao
                                    .getAllPlaylists()
                                    .mapFlat()
                                else playlistDao
                                    .getAllPlaylistsWithItems()
                                    .mapDeep()
                            is PlatformIdListFilter ->
                                if (flat) playlistDao
                                    .loadAllByPlatformIds(filter.ids)
                                    .mapFlat()
                                else playlistDao
                                    .loadAllByPlatformIdsWithItems(filter.ids)
                                    .mapDeep()
                            is ChannelPlatformIdFilter ->
                                if (flat) playlistDao
                                    .findPlaylistsForChannePlatformlId(filter.platformId)
                                    .mapFlat()
                                else throw IllegalArgumentException("Only flat supported for ChannelPlatformIdFilter")
                            else ->// todo return empty for else
                                playlistDao
                                    .getAllPlaylists()
                                    .mapFlat()
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
    suspend fun savePlaylistItem(item: PlaylistItemDomain, emit: Boolean = true, flat: Boolean = true): RepoResult<PlaylistItemDomain> =
        withContext(coProvider.IO) {
            try {
                item
                    .let { item ->
                        if (item.media.id == null || !flat) {
                            log.d("Save media check: ${item.media.platformId}")
                            val saved = mediaRepository.save(item.media)
                                .takeIf { it.isSuccessful }
                                ?.data
                                ?: throw java.lang.IllegalStateException("Save media failed ${item.media.platformId}")
                            item.copy(media = saved)
                        } else item
                    }
                    .let { item ->
                        log.d("Item media : ${item.media}")
                        item to playlistItemMapper.map(item)
                    }
                    .let { (domain, itemEntity) ->
                        domain to if (itemEntity.id != INITIAL_ID) {
                            // check record exists
                            playlistItemDao.load(itemEntity.id)?.run {
                                playlistItemDao.update(itemEntity); itemEntity
                            } ?: itemEntity.copy(id = playlistItemDao.insert(itemEntity))
                        } else {
                            itemEntity.copy(id = playlistItemDao.insert(itemEntity))
                        }
                    }
                    .let { (domain, itemEntity) -> playlistItemMapper.map(itemEntity, domain.media) }
                    .let { RepoResult.Data(it) }
                    .also { if (emit) it.data?.also { _playlistItemFlow.emit(FLAT to it) } }
            } catch (e: Throwable) {
                val msg = "couldn't save playlist item"
                log.e(msg, e)
                RepoResult.Error<PlaylistItemDomain>(e, msg)
            }
        }

    suspend fun savePlaylistItems(
        items: List<PlaylistItemDomain>,
        emit: Boolean = true,
        flat: Boolean = true
    ): RepoResult<List<PlaylistItemDomain>> =
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
                    .let { itemDomains ->
                        itemDomains.filter { it.media.id == null || !flat }
                            .takeIf { it.size > 0 }
                            ?.let {
                                mediaRepository.save(it.map { it.media }).data!!.associateBy { it.platformId }
                            }?.let { lookup ->
                                itemDomains.map { if (it.media.id == null) it.copy(media = lookup.get(it.media.platformId)!!) else it }
                            }
                            ?: itemDomains
                    }
                    .let { itemDomains -> itemDomains to itemDomains.map { playlistItemMapper.map(it) } }
                    .let { (domains, entities) ->
                        domains to entities.map {
                            if (it.id != INITIAL_ID) {
                                playlistItemDao.update(it); it
                            } else {
                                it.copy(id = playlistItemDao.insert(it))
                            }
                        }
                    }
                    .let { (domains, entities) ->
                        entities.map { savedItem ->
                            playlistItemMapper.map(
                                savedItem,
                                domains.find { it.media.id == savedItem.mediaId }
                                    ?.media
                                    ?: throw IllegalStateException("Media id saved incorrectly")
                            )
                        }
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
                    .let { playlistItemMapper.map(it, mediaRepository.load(it.mediaId).data!!) }
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
                    // todo load media list (dont map!)
                    is IdListFilter -> playlistItemDao.loadAllByIds(filter.ids)
                        .map { playlistItemMapper.map(it, mediaRepository.load(it.mediaId).data!!) }
                    is MediaIdListFilter -> playlistItemDao.loadItemsByMediaId(filter.ids)
                        .map { playlistItemMapper.map(it, mediaRepository.load(it.mediaId).data!!) }
                    is NewMediaFilter ->
                        playlistItemDao.loadAllPlaylistItemsWithNewMedia(200)
                            .map { playlistItemMapper.map(it) }
                    is RecentMediaFilter ->
                        playlistItemDao.loadAllPlaylistItemsRecent(200)
                            .map { playlistItemMapper.map(it) }
                    is SearchFilter ->
                        if (filter.playlistIds.isNullOrEmpty()) {
                            playlistItemDao.search(filter.text.toLowerCase(), 200)
                        } else {
                            playlistItemDao.search(filter.text.toLowerCase(), filter.playlistIds, 200)
                        }.map { playlistItemMapper.map(it) }
                    else -> playlistItemDao.loadAllItems()
                        .map { playlistItemMapper.map(it, mediaRepository.load(it.mediaId).data!!) }
                }.let { RepoResult.Data(it) }
            } catch (e: Throwable) {
                val msg = "couldn't load playlist item list for: $filter"
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

    override suspend fun update(update: UpdateObject<PlaylistDomain>, flat: Boolean, emit: Boolean): RepoResult<PlaylistDomain> {
        TODO("Not yet implemented")
    }


}