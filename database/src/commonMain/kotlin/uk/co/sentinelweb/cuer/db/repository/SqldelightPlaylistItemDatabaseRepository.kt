package uk.co.sentinelweb.cuer.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.db.repository.ConflictException
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistItemDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FLAT
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.db.mapper.PlaylistItemMapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class SqldelightPlaylistItemDatabaseRepository(
    private val database: Database,
    private val mediaDatabaseRepository: SqldelightMediaDatabaseRepository,
    private val playlistItemMapper: PlaylistItemMapper,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper,
) : PlaylistItemDatabaseRepository {

    init {
        log.tag(this)
    }

    val _updatesFlow = MutableSharedFlow<Pair<Operation, PlaylistItemDomain>>()
    override val updates: Flow<Pair<Operation, PlaylistItemDomain>>
        get() = _updatesFlow

    override val stats: Flow<Pair<Operation, Nothing>>
        get() = TODO("Not yet implemented")

    override suspend fun save(
        domain: PlaylistItemDomain,
        flat: Boolean,
        emit: Boolean
    ): RepoResult<PlaylistItemDomain> = withContext(coProvider.IO) {
        try {
            domain
                .let { item ->
                    if (item.media.id == null || !flat) {
                        log.d("Save media check: ${item.media.platformId}")
                        val saved = mediaDatabaseRepository.save(item.media, emit = emit, flat = false)
                            .takeIf { it.isSuccessful }
                            ?.data
                            ?: throw IllegalStateException("Save media failed ${item.media.platformId}")
                        item.copy(media = saved)
                    } else item
                }
                .let { item ->
                    item to playlistItemMapper.map(item)
                }
                .let { (domain, itemEntity) ->
                    with(database.playlistItemEntityQueries) {
                        val platformCheck = try {
                            loadItemsByMediaIdAndPlaylistId(
                                mediaId = itemEntity.media_id, playlistId = itemEntity.playlist_id
                            ).executeAsOne()
                        } catch (n: NullPointerException) {
                            null
                        }
                        domain to if (domain.id != null) {
                            if (platformCheck != null && platformCheck.id != itemEntity.id) {
                                throw ConflictException(
                                    "conflicting playlistitem id for ${itemEntity.media_id}_${itemEntity.playlist_id}" +
                                            " existing: ${platformCheck.id}  thisid:${itemEntity.id} "
                                )
                            }
                            itemEntity.also { update(it) }
                            load(itemEntity.id).executeAsOne()
                        } else {
                            if (platformCheck != null) {
                                throw ConflictException("playlistitem already exists: ${itemEntity.media_id}_${itemEntity.playlist_id}")
                            }
                            create(itemEntity)
                            itemEntity.copy(id = getInsertId().executeAsOne())
                        }
                    }
                }
                .let { (domain, itemEntity) -> playlistItemMapper.map(itemEntity, domain.media) }
                .let { RepoResult.Data(it) }
                .also {
                    if (emit) it.data
                        ?.also { _updatesFlow.emit((if (flat) FLAT else FULL) to it) }
                }
        } catch (e: Throwable) {
            val msg = "couldn't save playlist item"
            log.e(msg, e)
            RepoResult.Error<PlaylistItemDomain>(e, msg)
        }
    }

    override suspend fun save(
        domains: List<PlaylistItemDomain>,
        flat: Boolean,
        emit: Boolean
    ): RepoResult<List<PlaylistItemDomain>> = withContext(coProvider.IO) {
        database.playlistItemEntityQueries.transactionWithResult<RepoResult<List<PlaylistItemDomain>>> {
            try {
                saveListInternal(domains, flat)
            } catch (e: Throwable) {
                val msg = "Couldn't save playlist items"
                log.e(msg, e)
                rollback(RepoResult.Error<List<PlaylistItemDomain>>(e, msg))
            }
        }.also {
            it.takeIf { it.isSuccessful && emit }
                ?.data
                ?.map { _updatesFlow.emit((if (flat) FLAT else FULL) to it) }
        }
    }

    override suspend fun load(id: Long, flat: Boolean): RepoResult<PlaylistItemDomain> =
        withContext(coProvider.IO) {
            try {
                database.playlistItemEntityQueries.load(id)
                    .executeAsOne()
                    .let { playlistItemMapper.map(it, mediaDatabaseRepository.load(it.media_id, flat = false).data!!) }
                    .let { RepoResult.Data(it) }
            } catch (e: Throwable) {
                val msg = "couldn't load playlist item $id"
                log.e(msg, e)
                RepoResult.Error<PlaylistItemDomain>(e, msg)
            }
        }

    override suspend fun loadList(
        filter: Filter,
        flat: Boolean
    ): RepoResult<List<PlaylistItemDomain>> = withContext(coProvider.IO) {
        try {
            with(database.playlistItemEntityQueries) {
                when (filter) {
                    is AllFilter -> loadAll()
                    is IdListFilter -> loadAllByIds(filter.ids)
                    is PlaylistIdLFilter -> loadPlaylist(filter.id)
                    is MediaIdListFilter -> loadItemsByMediaId(filter.ids)
                    is NewMediaFilter -> loadAllPlaylistItemsWithNewMedia(filter.limit.toLong())
                    is RecentMediaFilter -> loadAllPlaylistItemsRecent(filter.limit.toLong())
                    is StarredMediaFilter -> loadAllPlaylistItemsStarred(filter.limit.toLong())
                    is UnfinishedMediaFilter -> loadAllPlaylistItemsUnfinished(
                        min_percent = filter.minPercent.toLong(),
                        max_percent = filter.maxPercent.toLong(),
                        limit = filter.limit.toLong()
                    )

                    is PlatformIdListFilter -> loadAllByPlatformIds(filter.ids)
                    is ChannelPlatformIdFilter -> findPlaylistItemsForChannelPlatformId(filter.platformId)
                    is SearchFilter -> {
                        val playlistIds = filter.playlistIds
                        if (playlistIds.isNullOrEmpty()) {
                            search(filter.text.lowercase(), 200)
                        } else {
                            searchPlaylists(filter.text.lowercase(), playlistIds, 200)
                        }
                    }

                    else -> throw IllegalArgumentException("filter not supported $filter")
                }.executeAsList()
                    .map {
                        playlistItemMapper.map(
                            it,
                            mediaDatabaseRepository.load(it.media_id, false).data!!
                        )
                    }
                    .let { RepoResult.Data(it) }
            }
        } catch (e: Throwable) {
            val msg = "couldn't load playlist item list for: $filter"
            log.e(msg, e)
            RepoResult.Error<List<PlaylistItemDomain>>(e, msg)
        }
    }

    override suspend fun loadStatsList(filter: Filter): RepoResult<List<Nothing>> {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: Filter): RepoResult<Int> = withContext(coProvider.IO) {
        try {
            when (filter) {
                is AllFilter -> RepoResult.Data(database.playlistItemEntityQueries.count().executeAsOne().toInt())
                else -> throw IllegalArgumentException("$filter not implemented")
            }
        } catch (e: Exception) {
            val msg = "couldn't count medias"
            log.e(msg, e)
            RepoResult.Error<Int>(e, msg)
        }
    }

    override suspend fun delete(domain: PlaylistItemDomain, emit: Boolean): RepoResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                domain
                    .let { playlistItemMapper.map(it) }
                    .also { database.playlistItemEntityQueries.delete(it.id) }
                    .also {
                        if (emit) {
                            _updatesFlow.emit(Operation.DELETE to domain)
                        }
                    }
                RepoResult.Data.Empty(true)
            } catch (e: Throwable) {
                val msg = "couldn't delete ${domain.id}"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }

    override suspend fun deleteAll(): RepoResult<Boolean> = withContext(coProvider.IO) {
        var result: RepoResult<Boolean> = RepoResult.Data(false)
        database.playlistItemEntityQueries.transaction {
            result = try {
                database.playlistItemEntityQueries
                    .deleteAll()
                RepoResult.Data(true)
            } catch (e: Throwable) {
                val msg = "couldn't deleteAll medias"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }
        result
    }

    override suspend fun update(
        update: UpdateDomain<PlaylistItemDomain>,
        flat: Boolean,
        emit: Boolean
    ): RepoResult<PlaylistItemDomain> {
        TODO("Not yet implemented")
    }

    internal fun saveListInternal(
        domains: List<PlaylistItemDomain>,
        flat: Boolean
    ): RepoResult<List<PlaylistItemDomain>> {
        return domains
            .let { itemDomains ->
                itemDomains
                    .filter { it.media.id == null || !flat }
                    .takeIf { it.size > 0 }
                    ?.let {
                        mediaDatabaseRepository
                            .saveMediasInternal(it.map { it.media }, false)
                            .associateBy { it.platformId }
                    }?.let { lookup ->
                        itemDomains.map {
                            if (it.media.id == null)
                                it.copy(media = lookup.get(it.media.platformId)!!)
                            else it
                        }
                    }
                    ?: itemDomains
            }
            .let { itemDomains -> itemDomains to itemDomains.map { playlistItemMapper.map(it) } }
            .let { (domains, entities) ->
                with(database.playlistItemEntityQueries) {
                    domains to entities.map { itemEntity ->
                        val platformCheck = try {
                            loadItemsByMediaIdAndPlaylistId(
                                mediaId = itemEntity.media_id, playlistId = itemEntity.playlist_id
                            ).executeAsOne()
                        } catch (n: NullPointerException) {
                            null
                        }
                        if (itemEntity.id > 0L) {
                            if (platformCheck != null && platformCheck.id != itemEntity.id) {
                                throw ConflictException(
                                    "conflicting playlistitem id for ${itemEntity.media_id}_${itemEntity.playlist_id}" +
                                            " existing: ${platformCheck.id}  thisid:${itemEntity.id} "
                                )
                            }
                            itemEntity.also { update(it) }
                        } else {
                            if (platformCheck != null) {
                                throw ConflictException("playlistitem already exists: ${itemEntity.media_id}_${itemEntity.playlist_id}")
                            }
                            create(itemEntity)
                            itemEntity.copy(id = getInsertId().executeAsOne())
                        }
                    }
                }
            }
            .let { (domains, entities) ->
                entities.map { savedItem ->
                    playlistItemMapper.map(
                        savedItem,
                        domains.find { it.media.id == savedItem.media_id }
                            ?.media
                            ?: throw IllegalStateException("Media id saved incorrectly")
                    )
                }
            }
            .let { RepoResult.Data(it) }
    }

    internal fun loadPlaylistItemsInternal(
        playlistId: Long
    ): List<PlaylistItemDomain> =
        database.playlistItemEntityQueries
            .loadPlaylist(playlistId)
            .executeAsList()
            .mapNotNull { itemEntity ->
                mediaDatabaseRepository
                    .loadMediaInternal(itemEntity.media_id).data
                    ?.let { mediaDomain -> playlistItemMapper.map(itemEntity, mediaDomain) }
                    ?: let { log.e("failed to load Media:id ${itemEntity.media_id}"); null }
            }
}
