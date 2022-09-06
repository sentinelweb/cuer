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
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
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
        try {
            saveInternal(domain, flat)
                .let { RepoResult.Data(load(id = it, flat).data) }
                .also {
                    if (emit) it.data
                        ?.also { _updatesFlow.emit((if (flat) FLAT else FULL) to it) }
                }
        } catch (e: Exception) {
            val msg = "couldn't save playlist: ${domain}"
            log.e(msg, e)
            RepoResult.Error<PlaylistDomain>(e, msg)
        }
    }

    override suspend fun save(
        domains: List<PlaylistDomain>,
        flat: Boolean,
        emit: Boolean
    ): RepoResult<List<PlaylistDomain>> = withContext(coProvider.IO) {
        // todo should be transactional - but its a lot of work ..
        database.playlistItemEntityQueries.transactionWithResult<RepoResult<List<PlaylistDomain>>> {

            try {
                domains
                    .map { saveInternal(it, flat) }
                    .let { RepoResult.Data(it.map { loadPlaylistInternal(id = it, flat).data!! }) }
            } catch (e: Exception) {
                val msg = "couldn't save playlists: ${domains}"
                log.e(msg, e)
                RepoResult.Error<List<PlaylistDomain>>(e, msg)
            }
        }
            .also {
                if (emit) it.data
                    ?.forEach { _updatesFlow.emit((if (flat) FLAT else FULL) to it) }
            }
    }

    override suspend fun load(id: Long, flat: Boolean): RepoResult<PlaylistDomain> = loadPlaylist(id, flat)

    override suspend fun loadList(
        filter: Filter?,
        flat: Boolean
    ): RepoResult<List<PlaylistDomain>> = withContext(coProvider.IO) {
        database.playlistItemEntityQueries.transactionWithResult<RepoResult<List<PlaylistDomain>>> {
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

    override suspend fun loadStatsList(filter: Filter?): RepoResult<List<PlaylistStatDomain>> {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: Filter?): RepoResult<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(domain: PlaylistDomain, emit: Boolean): RepoResult<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll(): RepoResult<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun update(
        update: UpdateDomain<PlaylistDomain>,
        flat: Boolean,
        emit: Boolean
    ): RepoResult<PlaylistDomain> {
        TODO("Not yet implemented")
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