package uk.co.sentinelweb.cuer.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.db.repository.ConflictException
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistItemDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FLAT
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.db.mapper.PlaylistItemMapper
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.domain.toGUID
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class SqldelightPlaylistItemDatabaseRepository(
    private val database: Database,
    private val mediaDatabaseRepository: SqldelightMediaDatabaseRepository,
    private val playlistItemMapper: PlaylistItemMapper,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper,
    private val guidCreator: GuidCreator,
    private val source: OrchestratorContract.Source,
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
                    // log.d("Save media check: flat: $flat, media.id=${item.media.id}, media.platformId=${item.media.platformId} media.duration=${item.media.duration}")
                    if (item.media.id?.source != source || !flat) {
                        //log.d("Save media check: ${item.media.platformId}")
                        val saved = mediaDatabaseRepository.save(item.media, emit = emit, flat = false)
                            .takeIf { it.isSuccessful }
                            ?.data
                            ?: throw IllegalStateException("Save media failed ${item.media.platformId}")
                        // log.d("Saved media check: flat: $flat, media.id=${saved.id}, media.platformId=${saved.platformId} media.duration=${saved.duration}")
                        item.copy(media = saved)
                    } else item
                }
                .let { toSaveDomain ->
                    with(database.playlistItemEntityQueries) {
                        val platformCheck = try {
                            loadItemsByMediaIdAndPlaylistId(
                                mediaId = toSaveDomain.media.id!!.id.value,
                                playlistId = toSaveDomain.playlistId!!.id.value
                            ).executeAsOne()
                        } catch (n: NullPointerException) {
                            null
                        }
                        toSaveDomain to if (toSaveDomain.id?.source == source) {
                            if (platformCheck != null && platformCheck.id != toSaveDomain.id!!.id.value) {
                                throw ConflictException(
                                    "conflicting playlistitem id for ${toSaveDomain.media.id}_${toSaveDomain.playlistId!!.id}" +
                                            " existing: ${platformCheck.id}  thisid: ${toSaveDomain.id} "
                                )
                            }
                            update(playlistItemMapper.map(toSaveDomain))
                            load(toSaveDomain.id!!.id.value).executeAsOne()
                        } else {
                            // fixme some logic to add here need to merge the entities or throw conflict
                            if (platformCheck != null) {
                                //throw ConflictException("playlistitem already exists: ${toSaveDomain.media.id}_${toSaveDomain.playlistId}")
                                platformCheck
                            } else {
                                guidCreator.create().toIdentifier(source)
                                    .let { playlistItemMapper.map(toSaveDomain.copy(id = it)) }
                                    .also { create(it) }
                            }
                        }
                    }
                }
                .let { (savedDomain, itemEntity) -> playlistItemMapper.map(itemEntity, savedDomain.media) }
                //.also { log.d("Saved media check: flat: $flat, media.id=${it.media.id}, media.platformId=${it.media.platformId} media.duration=${it.media.duration}") }
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

    override suspend fun load(id: GUID, flat: Boolean): RepoResult<PlaylistItemDomain> =
        withContext(coProvider.IO) {
            try {
                database.playlistItemEntityQueries.load(id.value)
                    .executeAsOne()
                    .let {
                        playlistItemMapper.map(
                            it,
                            mediaDatabaseRepository.load(it.media_id.toGUID(), flat = false).data!!
                        )
                    }
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
                    is IdListFilter -> loadAllByIds(filter.ids.map { it.value })
                    is PlaylistIdLFilter -> loadPlaylist(filter.id.value)
                    is MediaIdListFilter -> loadItemsByMediaId(filter.ids.map { it.value })
                    is NewMediaFilter -> loadAllPlaylistItemsWithNewMedia(filter.limit.toLong())
                    is RecentMediaFilter -> loadAllPlaylistItemsRecent(filter.limit.toLong())
                    is StarredMediaFilter -> loadAllPlaylistItemsStarred(filter.limit.toLong())
                    is LiveUpcomingMediaFilter -> loadAllPlaylistItemsLiveAndUpcoming(filter.limit.toLong())
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
                            searchPlaylists(filter.text.lowercase(), playlistIds.map { it.value }, 200)
                        }
                    }

                    else -> throw IllegalArgumentException("filter not supported $filter")
                }.executeAsList()
                    .map {
                        playlistItemMapper.map(
                            it,
                            mediaDatabaseRepository.load(it.media_id.toGUID(), false).data!!
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
                    .filter { it.media.id?.source != source || !flat }
                    .takeIf { it.size > 0 }
                    ?.let {
                        // save all the unsaved media items and make a lookup
                        mediaDatabaseRepository
                            .saveMediasInternal(it.map { it.media }, false)
                            .associateBy { it.platformId }
                    }?.let { lookup ->
                        itemDomains.map {
                            if (it.media.id?.source != source)
                                it.copy(media = lookup.get(it.media.platformId)!!)
                            else it
                        }
                    }
                    ?: itemDomains
            }
            //.let { itemDomains -> itemDomains to itemDomains.map { playlistItemMapper.map(it) } }
            .let { itemDomains ->
                with(database.playlistItemEntityQueries) {
                    itemDomains to itemDomains.map { itemDomain ->
                        val platformCheck = try {
                            loadItemsByMediaIdAndPlaylistId(
                                mediaId = itemDomain.media.id!!.id.value, playlistId = itemDomain.playlistId!!.id.value
                            ).executeAsOne()
                        } catch (n: NullPointerException) {
                            null
                        }
                        if (itemDomain.id != null) {
                            if (platformCheck != null && platformCheck.id != itemDomain.id!!.id.value) {
                                throw ConflictException(
                                    "conflicting playlistitem id for ${itemDomain.media.id!!.id.value}_${itemDomain.playlistId!!.id.value}" +
                                            " existing: ${platformCheck.id}  thisid:${itemDomain.id}"
                                )
                            }
                            playlistItemMapper.map(itemDomain).also { update(it) }
                        } else {
                            // fixme some logic to sort here possibly need to merge the entities
                            if (platformCheck != null) {
                                //throw ConflictException("playlistitem already exists: ${itemDomain.media.id!!.id.value}_${itemDomain.playlistId!!.id.value}")
                                platformCheck
                            } else {
                                guidCreator.create().toIdentifier(source)
                                    .let { playlistItemMapper.map(itemDomain.copy(id = it)) }
                                    .also { create(it) }
                            }
                        }
                    }
                }
            }
            .let { (domains, entities) ->
                entities.map { savedItem ->
                    playlistItemMapper.map(
                        savedItem,
                        domains.find { it.media.id!!.id.value == savedItem.media_id }
                            ?.media
                            ?: throw IllegalStateException("Media id saved incorrectly")
                    )
                }
            }
            .let { RepoResult.Data(it) }
    }

    internal fun loadPlaylistItemsInternal(
        playlistId: GUID
    ): List<PlaylistItemDomain> =
        database.playlistItemEntityQueries
            .loadPlaylist(playlistId.value)
            .executeAsList()
            .mapNotNull { itemEntity ->
                mediaDatabaseRepository
                    .loadMediaInternal(itemEntity.media_id.toGUID()).data
                    ?.let { mediaDomain -> playlistItemMapper.map(itemEntity, mediaDomain) }
                    ?: let { log.e("failed to load Media:id ${itemEntity.media_id}"); null }
            }
}
