package uk.co.sentinelweb.cuer.app.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.AppDatabase
import uk.co.sentinelweb.cuer.app.db.dao.PlaylistItemDao
import uk.co.sentinelweb.cuer.app.db.mapper.PlaylistItemMapper
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class RoomPlaylistItemDatabaseRepository constructor(
    private val playlistItemDao: PlaylistItemDao,
    private val playlistItemMapper: PlaylistItemMapper,
    private val roomMediaRepository: RoomMediaDatabaseRepository,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper,
    private val database: AppDatabase
) : PlaylistItemDatabaseRepository {
    private val _playlistItemFlow = MutableSharedFlow<Pair<OrchestratorContract.Operation, PlaylistItemDomain>>()
    override val updates: Flow<Pair<OrchestratorContract.Operation, PlaylistItemDomain>>
        get() = _playlistItemFlow

    override suspend fun save(domain: PlaylistItemDomain, flat: Boolean, emit: Boolean): RepoResult<PlaylistItemDomain> =
        savePlaylistItem(domain, emit, flat)

    override suspend fun save(domains: List<PlaylistItemDomain>, flat: Boolean, emit: Boolean): RepoResult<List<PlaylistItemDomain>> =
        savePlaylistItems(domains, emit, flat)

    override suspend fun load(id: Long, flat: Boolean): RepoResult<PlaylistItemDomain> =
        loadPlaylistItem(id)

    override suspend fun loadList(filter: OrchestratorContract.Filter?, flat: Boolean): RepoResult<List<PlaylistItemDomain>> =
        loadPlaylistItems(filter)

    override suspend fun count(filter: OrchestratorContract.Filter?): RepoResult<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll(): RepoResult<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun update(update: UpdateDomain<PlaylistItemDomain>, flat: Boolean, emit: Boolean): RepoResult<PlaylistItemDomain> {
        TODO("Not yet implemented")
    }


    // region PlaylistItemDomain
    suspend fun savePlaylistItem(item: PlaylistItemDomain, emit: Boolean = true, flat: Boolean = true): RepoResult<PlaylistItemDomain> =
        withContext(coProvider.IO) {
            try {
                item
                    .let { item ->
                        if (item.media.id == null || !flat) {
                            log.d("Save media check: ${item.media.platformId}")
                            val saved = roomMediaRepository.save(item.media)
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
                        domain to if (itemEntity.id != AppDatabase.INITIAL_ID) {
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
                    .also { if (emit) it.data?.also { _playlistItemFlow.emit(OrchestratorContract.Operation.FLAT to it) } }
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
                                roomMediaRepository.save(it.map { it.media }).data!!.associateBy { it.platformId }
                            }?.let { lookup ->
                                itemDomains.map { if (it.media.id == null) it.copy(media = lookup.get(it.media.platformId)!!) else it }
                            }
                            ?: itemDomains
                    }
                    .let { itemDomains -> itemDomains to itemDomains.map { playlistItemMapper.map(it) } }
                    .let { (domains, entities) ->
                        domains to entities.map {
                            if (it.id != AppDatabase.INITIAL_ID) {
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
                    .also { if (emit) it.data?.forEach { _playlistItemFlow.emit(OrchestratorContract.Operation.FLAT to it) } }
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
                    .let { playlistItemMapper.map(it, roomMediaRepository.load(it.mediaId).data!!) }
                    .let { RepoResult.Data(it) }
            } catch (e: Throwable) {
                val msg = "couldn't save playlist item"
                log.e(msg, e)
                RepoResult.Error<PlaylistItemDomain>(e, msg)
            }
        }

    suspend fun loadPlaylistItems(filter: OrchestratorContract.Filter? = null): RepoResult<List<PlaylistItemDomain>> =
        withContext(coProvider.IO) {
            try {
                when (filter) {
                    // todo load media list (dont map!)
                    is OrchestratorContract.IdListFilter -> playlistItemDao.loadAllByIds(filter.ids)
                        .map { playlistItemMapper.map(it, roomMediaRepository.load(it.mediaId).data!!) }
                    is OrchestratorContract.MediaIdListFilter -> playlistItemDao.loadItemsByMediaId(filter.ids)
                        .map { playlistItemMapper.map(it, roomMediaRepository.load(it.mediaId).data!!) }
                    is OrchestratorContract.NewMediaFilter ->
                        playlistItemDao.loadAllPlaylistItemsWithNewMedia(200)
                            .map { playlistItemMapper.map(it) }
                    is OrchestratorContract.RecentMediaFilter ->
                        playlistItemDao.loadAllPlaylistItemsRecent(200)
                            .map { playlistItemMapper.map(it) }
                    is OrchestratorContract.SearchFilter ->
                        if (filter.playlistIds.isNullOrEmpty()) {
                            playlistItemDao.search(filter.text.toLowerCase(), 200)
                        } else {
                            playlistItemDao.search(filter.text.toLowerCase(), filter.playlistIds, 200)
                        }.map { playlistItemMapper.map(it) }
                    else -> playlistItemDao.loadAllItems()
                        .map { playlistItemMapper.map(it, roomMediaRepository.load(it.mediaId).data!!) }
                }.let { RepoResult.Data(it) }
            } catch (e: Throwable) {
                val msg = "couldn't load playlist item list for: $filter"
                log.e(msg, e)
                RepoResult.Error<List<PlaylistItemDomain>>(e, msg)
            }
        }

    override suspend fun delete(domain: PlaylistItemDomain, emit: Boolean): RepoResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                domain
                    .let { playlistItemMapper.map(it) }
                    .also { playlistItemDao.delete(it) }
                    .also {
                        if (emit) {
                            _playlistItemFlow.emit(OrchestratorContract.Operation.DELETE to domain)
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
}