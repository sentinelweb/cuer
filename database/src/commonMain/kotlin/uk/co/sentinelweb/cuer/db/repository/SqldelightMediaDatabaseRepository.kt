package uk.co.sentinelweb.cuer.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FLAT
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.database.entity.Media
import uk.co.sentinelweb.cuer.db.mapper.MediaMapper
import uk.co.sentinelweb.cuer.db.update.MediaUpdateMapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.domain.update.MediaPositionUpdateDomain
import uk.co.sentinelweb.cuer.domain.update.MediaUpdateDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class SqldelightMediaDatabaseRepository(
    private val database: Database,
    private val imageDatabaseRepository: SqldelightImageDatabaseRepository,
    private val channelDatabaseRepository: SqldelightChannelDatabaseRepository,
    private val mediaMapper: MediaMapper,
    private val mediaUpdateMapper: MediaUpdateMapper,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper,
) : MediaDatabaseRepository {

    init {
        log.tag(this)
    }

    val _updatesFlow = MutableSharedFlow<Pair<Operation, MediaDomain>>()
    override val updates: Flow<Pair<Operation, MediaDomain>>
        get() = _updatesFlow

    override val stats: Flow<Pair<Operation, Nothing>>
        get() = TODO("Not yet implemented")

    override suspend fun save(domain: MediaDomain, flat: Boolean, emit: Boolean): RepoResult<MediaDomain> =
        withContext(coProvider.IO) {
            try {
                saveInternal(domain, flat)
                    .let { RepoResult.Data(load(id = it, flat).data) }
                    .also {
                        if (emit) it.data
                            ?.also { _updatesFlow.emit((if (flat) FLAT else FULL) to it) }
                    }
            } catch (e: Exception) {
                val msg = "couldn't save media: ${domain}"
                log.e(msg, e)
                RepoResult.Error<MediaDomain>(e, msg)
            }
        }

    override suspend fun save(domains: List<MediaDomain>, flat: Boolean, emit: Boolean): RepoResult<List<MediaDomain>> =
        withContext(coProvider.IO) {
            database.mediaEntityQueries.transactionWithResult<RepoResult<List<MediaDomain>>> {
                try {
                    saveMediasInternal(domains, flat)
                        .let { RepoResult.Data(it) }
                } catch (e: Exception) {
                    val msg = "couldn't save medias"
                    log.e(msg, e)
                    rollback(RepoResult.Error<List<MediaDomain>>(e, msg))
                }
            }.also {
                it.takeIf { it.isSuccessful && emit }
                    ?.data
                    ?.map { _updatesFlow.emit((if (flat) FLAT else FULL) to it) }
            }
        }

    override suspend fun load(id: Long, flat: Boolean): RepoResult<MediaDomain> = loadMedia(id)

    override suspend fun loadList(filter: Filter?, flat: Boolean): RepoResult<List<MediaDomain>> =
        withContext(coProvider.IO) {
            database.mediaEntityQueries.transactionWithResult<RepoResult<List<MediaDomain>>> {
                try {
                    when (filter) {
                        is IdListFilter ->
                            database.mediaEntityQueries
                                .loadAllByIds(filter.ids)
                                .executeAsList()
                                .map { fillAndMapEntity(it) }
                                .let { RepoResult.Data(it) }

                        is PlatformIdListFilter ->
                            filter.ids
                                // todo make query to load all at once
                                .mapNotNull {
                                    database.mediaEntityQueries
                                        .loadByPlatformId(it, YOUTUBE)
                                        .executeAsOneOrNull()
                                }
                                .map { fillAndMapEntity(it) }
                                .let { RepoResult.Data.dataOrEmpty(it) }

                        is ChannelPlatformIdFilter -> TODO()
                        else -> throw IllegalArgumentException("$filter not implemented")
                    }
                } catch (e: Exception) {
                    val msg = "couldn't load medias"
                    log.e(msg, e)
                    RepoResult.Error<List<MediaDomain>>(e, msg)
                }
            }
        }

    override suspend fun loadStatsList(filter: Filter?): RepoResult<List<Nothing>> {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: Filter?): RepoResult<Int> = withContext(coProvider.IO) {
        try {
            when (filter) {
                is AllFilter, null -> RepoResult.Data(database.mediaEntityQueries.count().executeAsOne().toInt())
                else -> throw IllegalArgumentException("$filter not implemented")
            }
        } catch (e: Exception) {
            val msg = "couldn't count medias"
            log.e(msg, e)
            RepoResult.Error<Int>(e, msg)
        }
    }

    override suspend fun delete(domain: MediaDomain, emit: Boolean): RepoResult<Boolean> = withContext(coProvider.IO) {
        try {
            domain.id
                ?.let { database.mediaEntityQueries.delete(it) }
                ?.let { RepoResult.Data(true) }
                ?.also { if (emit) _updatesFlow.emit(Operation.DELETE to domain) }
                ?: let { RepoResult.Data(false) }
        } catch (e: Exception) {
            val msg = "couldn't delete medias"
            log.e(msg, e)
            RepoResult.Error<Boolean>(e, msg)
        }
    }

    override suspend fun deleteAll(): RepoResult<Boolean> = withContext(coProvider.IO) {
        var result: RepoResult<Boolean> = RepoResult.Data(false)
        database.mediaEntityQueries.transaction {
            result = try {
                database.mediaEntityQueries
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
        update: UpdateDomain<MediaDomain>,
        flat: Boolean,
        emit: Boolean
    ): RepoResult<MediaDomain> = withContext(coProvider.IO) {
        try {
            when (update as? MediaUpdateDomain) {
                is MediaPositionUpdateDomain ->
                    (update as MediaPositionUpdateDomain)
                        .let { it to database.mediaEntityQueries.loadFlags(it.id).executeAsOne() }
                        .let { mediaUpdateMapper.map(it.first, it.second) }
                        .also {
                            database.mediaEntityQueries.updatePosition(
                                id = it.id,
                                dateLastPlayed = it.dateLastPlayed,
                                position = it.positon,
                                duration = it.duration,
                                flags = it.flags
                            )
                        }
                        .let { RepoResult.Data(load(id = it.id, flat).data) }
                        .also { if (emit) it.data?.also { _updatesFlow.emit((if (flat) FLAT else FULL) to it) } }

                else -> throw IllegalArgumentException("update object not valid: $update")
            }
        } catch (e: Throwable) {
            val msg = "couldn't update media: $update"
            log.e(msg, e)
            RepoResult.Error<MediaDomain>(e, msg)
        }
    }

    private suspend fun loadMedia(id: Long): RepoResult<MediaDomain> =
        withContext(coProvider.IO) {
            loadMediaInternal(id)
        }

    internal fun loadMediaInternal(id: Long) = try {
        database.mediaEntityQueries
            .loadById(id)
            .executeAsOne()
            .let { media: Media -> fillAndMapEntity(media) }
            .let { media: MediaDomain -> RepoResult.Data(media) }
    } catch (e: Throwable) {
        val msg = "couldn't load media:$id"
        log.e(msg, e)
        RepoResult.Error<MediaDomain>(e, msg)
    }

    private fun fillAndMapEntity(media: Media): MediaDomain = mediaMapper.map(
        media,
        channelDatabaseRepository.loadChannelInternal(media.channel_id!!).data!!,
        imageDatabaseRepository.loadEntity(media.thumb_id),
        imageDatabaseRepository.loadEntity(media.image_id),
    )

    internal fun saveMediasInternal(
        domains: List<MediaDomain>,
        flat: Boolean
    ) = domains
        .map { saveInternal(it, flat) }
        .map { loadMediaInternal(it).data!! }

    private fun saveInternal(mediaDomain: MediaDomain, flat: Boolean): Long =
        mediaDomain
            .let {
                if (!flat) it.copy(
                    channelData = channelDatabaseRepository.checkToSaveChannelInternal(it.channelData)
                )
                else it
            }
            .let {
                it.copy(thumbNail = it.thumbNail
                    ?.let { imageDatabaseRepository.checkToSaveImage(it) })
            }
            .let {
                it.copy(image = it.image
                    ?.let { imageDatabaseRepository.checkToSaveImage(it) })
            }
            .let {
                val mediaEntity = mediaMapper.map(it)
                with(database.mediaEntityQueries) {
                    if (mediaEntity.id > 0) {
                        update(mediaEntity)
                        mediaDomain.id!!
                    } else {
                        create(mediaEntity)
                        getInsertId().executeAsOne()
                    }
                }
            }
}