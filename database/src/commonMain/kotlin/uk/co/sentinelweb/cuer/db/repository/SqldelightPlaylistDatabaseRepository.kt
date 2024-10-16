package uk.co.sentinelweb.cuer.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.DbResult
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FLAT
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.database.entity.Playlist
import uk.co.sentinelweb.cuer.db.mapper.PlaylistMapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_WATCHED
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.domain.ext.summarise
import uk.co.sentinelweb.cuer.domain.update.PlaylistIndexUpdateDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class SqldelightPlaylistDatabaseRepository(
    private val database: Database,
    private val channelDatabaseRepository: SqldelightChannelDatabaseRepository,
    private val imageDatabaseRepository: SqldelightImageDatabaseRepository,
    private val itemDatabaseRepository: SqldelightPlaylistItemDatabaseRepository,
    private val playlistMapper: PlaylistMapper,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper,
    private val guidCreator: GuidCreator,
    private val source: OrchestratorContract.Source,
) : PlaylistDatabaseRepository {

    init {
        log.tag(this)
    }

    val _updatesFlow = MutableSharedFlow<Pair<Operation, PlaylistDomain>>()
    override val updates: Flow<Pair<Operation, PlaylistDomain>>
        get() = _updatesFlow

    override val stats: Flow<Pair<Operation, Nothing>>
        get() = TODO("Not yet implemented")

    override suspend fun save(
        domain: PlaylistDomain,
        flat: Boolean,
        emit: Boolean
    ): DbResult<PlaylistDomain> = withContext(coProvider.IO) {
        database.playlistItemEntityQueries.transactionWithResult<DbResult<PlaylistDomain>> {
            try {
                saveInternal(domain, flat)
                    .let { DbResult.Data(loadPlaylistInternal(id = it, flat).data) }

            } catch (e: Exception) {
                val msg = "couldn't save playlist: ${domain.summarise()}"
                log.e(msg, e)
                rollback(DbResult.Error<PlaylistDomain>(e, msg))
            }
        }.also {
            it
                .takeIf { it.isSuccessful && emit }
                ?.data
                ?.also { _updatesFlow.emit((if (flat) FLAT else FULL) to it) }
        }
    }

    override suspend fun save(
        domains: List<PlaylistDomain>,
        flat: Boolean,
        emit: Boolean
    ): DbResult<List<PlaylistDomain>> = withContext(coProvider.IO) {
        database.playlistItemEntityQueries.transactionWithResult<DbResult<List<PlaylistDomain>>> {
            try {
                domains
                    .map { saveInternal(it, flat) }
                    .let { DbResult.Data(it.map { loadPlaylistInternal(id = it, flat).data!! }) }
            } catch (e: Exception) {
                val msg = "couldn't save playlists: ${domains.map { it.summarise() }}"
                log.e(msg, e)
                rollback(DbResult.Error<List<PlaylistDomain>>(e, msg))
            }
        }.also {
            it
                .takeIf { it.isSuccessful && emit }
                ?.data
                ?.forEach { _updatesFlow.emit((if (flat) FLAT else FULL) to it) }
        }
    }

    override suspend fun load(id: GUID, flat: Boolean): DbResult<PlaylistDomain> = loadPlaylist(id, flat)

    override suspend fun loadList(
        filter: Filter,
        flat: Boolean
    ): DbResult<List<PlaylistDomain>> = withContext(coProvider.IO) {
        database.playlistItemEntityQueries.transactionWithResult {
            try {
                with(database.playlistEntityQueries) {
                    when (filter) {
                        is IdListFilter -> loadAllByIds(filter.ids.map { it.value })
                        is DefaultFilter -> loadAllByFlags(PlaylistDomain.FLAG_DEFAULT)
                        is AllFilter -> loadAll()
                        is PlatformIdListFilter -> loadAllByPlatformIds(filter.ids, filter.platform)
                        is ChannelPlatformIdFilter -> findPlaylistsForChannelPlatformId(filter.platformId)
                        is TitleFilter -> findPlaylistsWithTitle(filter.title)
                        else -> throw IllegalArgumentException("filter not supported $filter")
                    }
                }.executeAsList()
                    .map { entity ->
                        fillAndMapEntity(
                            entity,
                            if (flat) listOf()
                            else itemDatabaseRepository.loadPlaylistItemsInternal(entity.id.toGUID())
                        )
                    }
                    .let { DbResult.Data(it) }
            } catch (e: Exception) {
                val msg = "couldn't load playlists: ${filter}"
                log.e(msg, e)
                DbResult.Error<List<PlaylistDomain>>(e, msg)
            }
        }
    }

    override suspend fun loadStatsList(filter: Filter): DbResult<List<PlaylistStatDomain>> =
        withContext(coProvider.IO) {
            database.playlistItemEntityQueries.transactionWithResult {
                try {
                    when (filter) {
                        is IdListFilter ->
                            DbResult.Data(
                                filter.ids.map {
                                    PlaylistStatDomain(
                                        playlistId = it.toIdentifier(source),
                                        itemCount = database.playlistItemEntityQueries
                                            .countItemsInPlaylist(it.value).executeAsOne().toInt(),
                                        watchedItemCount = database.playlistItemEntityQueries
                                            .countMediaFlags(it.value, FLAG_WATCHED).executeAsOne().toInt()
                                    )
                                })

                        else -> throw UnsupportedOperationException("$filter not supported")
                    }

                } catch (e: Throwable) {
                    val msg = "couldn't load playlist stats "
                    log.e(msg, e)
                    DbResult.Error<List<PlaylistStatDomain>>(e, msg)
                }
            }
        }

    override suspend fun count(filter: Filter): DbResult<Int> =
        try {
            withContext(coProvider.IO) {
                database.playlistEntityQueries.count().executeAsOne().toInt()
            }.let { DbResult.Data(it) }
        } catch (e: Exception) {
            val msg = "couldn't count playlists ${filter}"
            log.e(msg, e)
            DbResult.Error<Int>(e, msg)
        }

    override suspend fun delete(domain: PlaylistDomain, emit: Boolean): DbResult<Boolean> =
        withContext(coProvider.IO) {
            try {
                domain
                    .let { playlistMapper.map(it) }
                    .also { database.playlistEntityQueries.delete(it.id) }
                    .also {
                        if (emit) {
                            _updatesFlow.emit(Operation.DELETE to domain)
                        }
                    }
                DbResult.Data.Empty(true)
            } catch (e: Throwable) {
                val msg = "couldn't delete ${domain.id}"
                log.e(msg, e)
                DbResult.Error<Boolean>(e, msg)
            }
        }

    override suspend fun deleteAll(): DbResult<Boolean> = withContext(coProvider.IO) {
        try {
            database.playlistEntityQueries.deleteAll()
            DbResult.Data.Empty(true)
        } catch (e: Exception) {
            val msg = "couldn't delete all media"
            log.e(msg, e)
            DbResult.Error<Boolean>(e, msg)
        }
    }


    override suspend fun update(
        update: UpdateDomain<PlaylistDomain>,
        flat: Boolean,
        emit: Boolean
    ): DbResult<PlaylistDomain> = withContext(coProvider.IO) {
        try {
            when (update) {
                is PlaylistIndexUpdateDomain -> updateCurrentIndex(update, emit)
                else -> throw IllegalArgumentException("update object not valid: ${update::class.simpleName}")
            }
        } catch (e: Throwable) {
            val msg = "couldn't update playlist $update"
            log.e(msg, e)
            DbResult.Error<PlaylistDomain>(e, msg)
        }
    }

    private suspend fun updateCurrentIndex(
        update: PlaylistIndexUpdateDomain,
        emit: Boolean = true
    ): DbResult<PlaylistDomain> = withContext(coProvider.IO) {
        try {
            update.id
                .let {
                    database.playlistEntityQueries
                        .updateIndex(update.currentIndex.toLong(), it.id.value); it
                }
                .let { load(it.id, flat = true) }
                .also {
                    if (emit) {
                        _updatesFlow.emit(FLAT to it.data!!)
                    }
                }
        } catch (e: Exception) {
            val msg = "couldn't update current index $update"
            log.e(msg, e)
            DbResult.Error<PlaylistDomain>(e, msg)
        }
    }

    private suspend fun loadPlaylist(id: GUID, flat: Boolean): DbResult<PlaylistDomain> =
        withContext(coProvider.IO) {
            loadPlaylistInternal(id, flat)
        }

    private fun loadPlaylistInternal(id: GUID, flat: Boolean) =
        try {
            database.playlistEntityQueries
                .load(id.value)
                .executeAsOneOrNull()!!
                .let { entity: Playlist ->
                    fillAndMapEntity(
                        entity,
                        if (flat) listOf()
                        else itemDatabaseRepository.loadPlaylistItemsInternal(entity.id.toGUID())
                    )
                }
                .let { domain: PlaylistDomain -> DbResult.Data(domain) }
        } catch (e: Throwable) {
            val msg = "couldn't load playlist: $id"
            log.e(msg, e)
            DbResult.Error<PlaylistDomain>(e, msg)
        }


    private fun fillAndMapEntity(
        playlist: Playlist,
        items: List<PlaylistItemDomain>
    ): PlaylistDomain = playlistMapper.map(
        playlist,
        items,
        playlist.channel_id?.let { channelDatabaseRepository.loadChannelInternal(it.toGUID()).data!! },
        imageDatabaseRepository.loadEntity(playlist.thumb_id?.toGUID()),
        imageDatabaseRepository.loadEntity(playlist.image_id?.toGUID()),
    )

    private fun saveInternal(domain: PlaylistDomain, flat: Boolean): GUID =
        domain
            .let {
                it.copy(channelData = it.channelData
                    ?.let { channelDatabaseRepository.checkToSaveChannelInternal(it) })
            }
            .let {
                it.copy(thumb = it.thumb?.let { imageDatabaseRepository.checkToSaveImage(it) })
            }
            .let {
                it.copy(image = it.image?.let { imageDatabaseRepository.checkToSaveImage(it) })
            }
            .let { toSaveDomain ->
                with(database.playlistEntityQueries) {
                    if (toSaveDomain.id?.source == source) {
                        val playlistEntity = playlistMapper.map(toSaveDomain)
                        update(playlistEntity)
                        toSaveDomain.id!!.id
                    } else {
                        guidCreator.create().toIdentifier(source)
                            .let { playlistMapper.map(toSaveDomain.copy(id = it)) }
                            .also { create(it) }
                            .id.toGUID()
                    }
                }
            }.also { playlistId ->
                if (!flat) {
                    itemDatabaseRepository.saveListInternal(
                        domain.items.map { it.copy(playlistId = playlistId.toIdentifier(source)) },
                        true
                    )
                }
            }
}


