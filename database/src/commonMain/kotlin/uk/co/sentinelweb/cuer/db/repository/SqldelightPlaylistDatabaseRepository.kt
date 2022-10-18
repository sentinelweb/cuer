package uk.co.sentinelweb.cuer.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FLAT
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.database.entity.Playlist
import uk.co.sentinelweb.cuer.db.mapper.PlaylistMapper
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_WATCHED
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
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
    ): RepoResult<PlaylistDomain> = withContext(coProvider.IO) {
        database.playlistItemEntityQueries.transactionWithResult<RepoResult<PlaylistDomain>> {
            try {
                saveInternal(domain, flat)
                    .let { RepoResult.Data(loadPlaylistInternal(id = it, flat).data) }

            } catch (e: Exception) {
                val msg = "couldn't save playlist: ${domain.pretty()}"
                log.e(msg, e)
                rollback(RepoResult.Error<PlaylistDomain>(e, msg))
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
    ): RepoResult<List<PlaylistDomain>> = withContext(coProvider.IO) {
        database.playlistItemEntityQueries.transactionWithResult<RepoResult<List<PlaylistDomain>>> {
            try {
                domains
                    .map { saveInternal(it, flat) }
                    .let { RepoResult.Data(it.map { loadPlaylistInternal(id = it, flat).data!! }) }
            } catch (e: Exception) {
                val msg = "couldn't save playlists: ${domains}"
                log.e(msg, e)
                rollback(RepoResult.Error<List<PlaylistDomain>>(e, msg))
            }
        }.also {
            it
                .takeIf { it.isSuccessful && emit }
                ?.data
                ?.forEach { _updatesFlow.emit((if (flat) FLAT else FULL) to it) }
        }
    }

    override suspend fun load(id: Long, flat: Boolean): RepoResult<PlaylistDomain> = loadPlaylist(id, flat)

    override suspend fun loadList(
        filter: Filter?,
        flat: Boolean
    ): RepoResult<List<PlaylistDomain>> = withContext(coProvider.IO) {
        database.playlistItemEntityQueries.transactionWithResult {
            try {
                with(database.playlistEntityQueries) {
                    when (filter) {
                        is IdListFilter -> loadAllByIds(filter.ids)
                        is DefaultFilter -> loadAllByFlags(PlaylistDomain.FLAG_DEFAULT)
                        is AllFilter -> loadAll()
                        is PlatformIdListFilter -> loadAllByPlatformIds(filter.ids)
                        is ChannelPlatformIdFilter -> findPlaylistsForChannelPlatformId(filter.platformId)
                        is TitleFilter -> findPlaylistsWithTitle(filter.title)
                        else ->// todo return empty for else
                            loadAll()
                    }
                }.executeAsList()
                    .map { entity ->
                        fillAndMapEntity(
                            entity,
                            if (flat) listOf()
                            else itemDatabaseRepository.loadPlaylistItemsInternal(entity.id)
                        )
                    }
                    .let { RepoResult.Data(it) }
            } catch (e: Exception) {
                val msg = "couldn't load playlists: ${filter}"
                log.e(msg, e)
                RepoResult.Error<List<PlaylistDomain>>(e, msg)
            }
        }
    }

    override suspend fun loadStatsList(filter: Filter?): RepoResult<List<PlaylistStatDomain>> =
        withContext(coProvider.IO) {
            database.playlistItemEntityQueries.transactionWithResult {
                try {
                    when (filter) {
                        is IdListFilter ->
                            RepoResult.Data(
                                filter.ids.map {
                                    PlaylistStatDomain(
                                        playlistId = it,
                                        itemCount = database.playlistItemEntityQueries
                                            .countItemsInPlaylist(it).executeAsOne().toInt(),
                                        watchedItemCount = database.playlistItemEntityQueries
                                            .countMediaFlags(it, FLAG_WATCHED).executeAsOne().toInt()
                                    )
                                })

                        else -> throw UnsupportedOperationException("$filter not supported")
                    }

                } catch (e: Throwable) {
                    val msg = "couldn't load playlist stats "
                    log.e(msg, e)
                    RepoResult.Error<List<PlaylistStatDomain>>(e, msg)
                }
            }
        }

    override suspend fun count(filter: Filter?): RepoResult<Int> =
        try {
            withContext(coProvider.IO) {
                database.playlistEntityQueries.count().executeAsOne().toInt()
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
                    .let { playlistMapper.map(it) }
                    .also { database.playlistEntityQueries.delete(it.id) }
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
        try {
            database.playlistEntityQueries.deleteAll()
            RepoResult.Data.Empty(true)
        } catch (e: Exception) {
            val msg = "couldn't delete all media"
            log.e(msg, e)
            RepoResult.Error<Boolean>(e, msg)
        }
    }


    override suspend fun update(
        update: UpdateDomain<PlaylistDomain>,
        flat: Boolean,
        emit: Boolean
    ): RepoResult<PlaylistDomain> = withContext(coProvider.IO) {
        try {
            when (update) {
                is PlaylistIndexUpdateDomain -> updateCurrentIndex(update, emit)
                else -> throw IllegalArgumentException("update object not valid: ${update::class.simpleName}")
            }
        } catch (e: Throwable) {
            val msg = "couldn't update playlist $update"
            log.e(msg, e)
            RepoResult.Error<PlaylistDomain>(e, msg)
        }
    }

    private suspend fun updateCurrentIndex(
        update: PlaylistIndexUpdateDomain,
        emit: Boolean = true
    ): RepoResult<PlaylistDomain> = withContext(coProvider.IO) {
        try {
            update.id
                .let {
                    database.playlistEntityQueries
                        .updateIndex(update.currentIndex.toLong(), it); it
                }
                .let { load(it, flat = true) }
                .also {
                    if (emit) {
                        _updatesFlow.emit(FLAT to it.data!!)
                    }
                }
        } catch (e: Exception) {
            val msg = "couldn't update current index $update"
            log.e(msg, e)
            RepoResult.Error<PlaylistDomain>(e, msg)
        }
    }

    private suspend fun loadPlaylist(id: Long, flat: Boolean): RepoResult<PlaylistDomain> =
        withContext(coProvider.IO) {
            loadPlaylistInternal(id, flat)
        }

    private fun loadPlaylistInternal(id: Long, flat: Boolean) =
        try {
            database.playlistEntityQueries
                .load(id)
                .executeAsOneOrNull()!!
                .let { entity: Playlist ->
                    fillAndMapEntity(
                        entity,
                        if (flat) listOf()
                        else itemDatabaseRepository.loadPlaylistItemsInternal(entity.id)
                    )
                }
                .let { domain: PlaylistDomain -> RepoResult.Data(domain) }
        } catch (e: Throwable) {
            val msg = "couldn't load playlist: $id"
            log.e(msg, e)
            RepoResult.Error<PlaylistDomain>(e, msg)
        }


    private fun fillAndMapEntity(
        playlist: Playlist,
        items: List<PlaylistItemDomain>
    ): PlaylistDomain = playlistMapper.map(
        playlist,
        items,
        playlist.channel_id?.let { channelDatabaseRepository.loadChannelInternal(it).data!! },
        imageDatabaseRepository.loadEntity(playlist.thumb_id),
        imageDatabaseRepository.loadEntity(playlist.image_id),
    )

    private fun saveInternal(domain: PlaylistDomain, flat: Boolean): Long =
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
            .let {
                val playlistEntity = playlistMapper.map(it)
                with(database.playlistEntityQueries) {
                    if (playlistEntity.id > 0) {
                        update(playlistEntity)
                        domain.id!!
                    } else {
                        create(playlistEntity)
                        getInsertId().executeAsOne()
                    }
                }
            }.also { playlistId ->
                if (!flat) {
                    itemDatabaseRepository.saveListInternal(
                        domain.items.map { it.copy(playlistId = playlistId) }, flat
                    )
                }
            }
}

private fun PlaylistDomain.pretty(): String {
    TODO("Not yet implemented")
}
