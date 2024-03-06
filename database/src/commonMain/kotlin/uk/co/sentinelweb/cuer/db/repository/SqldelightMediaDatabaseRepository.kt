package uk.co.sentinelweb.cuer.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.db.repository.ConflictException
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.DbResult
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FLAT
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.database.entity.Media
import uk.co.sentinelweb.cuer.db.mapper.MediaMapper
import uk.co.sentinelweb.cuer.db.update.MediaUpdateMapper
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.domain.toGUID
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
    private val guidCreator: GuidCreator,
    private val source: Source,
) : MediaDatabaseRepository {

    init {
        log.tag(this)
    }

    val _updatesFlow = MutableSharedFlow<Pair<Operation, MediaDomain>>()
    override val updates: Flow<Pair<Operation, MediaDomain>>
        get() = _updatesFlow

    override val stats: Flow<Pair<Operation, Nothing>>
        get() = TODO("Not yet implemented")

    override suspend fun save(domain: MediaDomain, flat: Boolean, emit: Boolean): DbResult<MediaDomain> =
        withContext(coProvider.IO) {
            try {
                //log.d("Media save: flat: $flat, media.id=${domain.id}, media.platformId=${domain.platformId} media.duration=${domain.duration}")
                val id = saveInternal(domain, flat)
                val loadResult = load(id = id, flat)
                val result = loadResult.data
                //log.d("Media load: flat: $flat, media.id=${result?.id}, media.platformId=${result?.platformId} media.duration=${result?.duration}")
                if (loadResult.isSuccessful)
                    DbResult.Data(loadResult.data)
                        .also {
                            if (emit) it.data
                                ?.also { _updatesFlow.emit((if (flat) FLAT else FULL) to it) }
                        }
                else loadResult
            } catch (e: Exception) {
                val msg = "couldn't save media: ${domain}"
                log.e(msg, e)
                DbResult.Error<MediaDomain>(e, msg)
            }
        }

    override suspend fun save(domains: List<MediaDomain>, flat: Boolean, emit: Boolean): DbResult<List<MediaDomain>> =
        withContext(coProvider.IO) {
            database.mediaEntityQueries.transactionWithResult<DbResult<List<MediaDomain>>> {
                try {
                    saveMediasInternal(domains, flat)
                        .let { DbResult.Data(it) }
                } catch (e: Exception) {
                    val msg = "couldn't save medias"
                    log.e(msg, e)
                    rollback(DbResult.Error<List<MediaDomain>>(e, msg))
                }
            }.also {
                it.takeIf { it.isSuccessful && emit }
                    ?.data
                    ?.map { _updatesFlow.emit((if (flat) FLAT else FULL) to it) }
            }
        }

    override suspend fun load(id: GUID, flat: Boolean): DbResult<MediaDomain> = loadMedia(id)

    override suspend fun loadList(filter: Filter, flat: Boolean): DbResult<List<MediaDomain>> =
        withContext(coProvider.IO) {
            database.mediaEntityQueries.transactionWithResult<DbResult<List<MediaDomain>>> {
                try {
                    with(database.mediaEntityQueries) {
                        when (filter) {
                            is AllFilter ->
                                loadAll().executeAsList()

                            is IdListFilter ->
                                loadAllByIds(filter.ids.map { it.value }).executeAsList()

                            is PlatformIdListFilter ->
                                filter.ids
                                    // todo make query to load all at once
                                    .mapNotNull {
                                        database.mediaEntityQueries
                                            .loadByPlatformId(it, YOUTUBE)
                                            .executeAsOneOrNull()
                                    }

                            is ChannelPlatformIdFilter -> TODO()
                            else -> throw IllegalArgumentException("$filter not implemented")
                        }
                            .map { fillAndMapEntity(it) }
                            .let { DbResult.Data.dataOrEmpty(it) }
                    }
                } catch (e: Exception) {
                    val msg = "couldn't load medias"
                    log.e(msg, e)
                    DbResult.Error<List<MediaDomain>>(e, msg)
                }
            }
        }

    override suspend fun loadStatsList(filter: Filter): DbResult<List<Nothing>> {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: Filter): DbResult<Int> = withContext(coProvider.IO) {
        try {
            when (filter) {
                is AllFilter -> DbResult.Data(database.mediaEntityQueries.count().executeAsOne().toInt())
                else -> throw IllegalArgumentException("$filter not implemented")
            }
        } catch (e: Exception) {
            val msg = "couldn't count medias"
            log.e(msg, e)
            DbResult.Error<Int>(e, msg)
        }
    }

    override suspend fun delete(domain: MediaDomain, emit: Boolean): DbResult<Boolean> = withContext(coProvider.IO) {
        try {
            domain.id
                ?.let { database.mediaEntityQueries.delete(it.id.value) }
                ?.let { DbResult.Data(true) }
                ?.also { if (emit) _updatesFlow.emit(Operation.DELETE to domain) }
                ?: let { DbResult.Data(false) }
        } catch (e: Exception) {
            val msg = "couldn't delete medias"
            log.e(msg, e)
            DbResult.Error<Boolean>(e, msg)
        }
    }

    override suspend fun deleteAll(): DbResult<Boolean> = withContext(coProvider.IO) {
        var result: DbResult<Boolean> = DbResult.Data(false)
        database.mediaEntityQueries.transaction {
            result = try {
                database.mediaEntityQueries
                    .deleteAll()
                DbResult.Data(true)
            } catch (e: Throwable) {
                val msg = "couldn't deleteAll medias"
                log.e(msg, e)
                DbResult.Error<Boolean>(e, msg)
            }
        }
        result
    }

    override suspend fun update(
        update: UpdateDomain<MediaDomain>,
        flat: Boolean,
        emit: Boolean
    ): DbResult<MediaDomain> = withContext(coProvider.IO) {
        try {
            when (update as? MediaUpdateDomain) {
                is MediaPositionUpdateDomain ->
                    (update as MediaPositionUpdateDomain)
                        .let { it to database.mediaEntityQueries.loadFlags(it.id.id.value).executeAsOne() }
                        .let { mediaUpdateMapper.map(it.first, it.second) }
                        .also {
                            database.mediaEntityQueries.updatePosition(
                                id = it.id.value,
                                dateLastPlayed = it.dateLastPlayed,
                                position = it.positon,
                                duration = it.duration,
                                flags = it.flags
                            )
                        }
                        .let { DbResult.Data(load(id = it.id, flat).data) }
                        .also { if (emit) it.data?.also { _updatesFlow.emit((if (flat) FLAT else FULL) to it) } }

                else -> throw IllegalArgumentException("update object not valid: $update")
            }
        } catch (e: Throwable) {
            val msg = "couldn't update media: $update"
            log.e(msg, e)
            DbResult.Error<MediaDomain>(e, msg)
        }
    }

    private suspend fun loadMedia(id: GUID): DbResult<MediaDomain> =
        withContext(coProvider.IO) {
            loadMediaInternal(id)
        }

    internal fun loadMediaInternal(id: GUID) = try {
        database.mediaEntityQueries
            .loadById(id.value)
            .executeAsOne()
            .let { media: Media -> fillAndMapEntity(media) }
            .let { media: MediaDomain -> DbResult.Data(media) }
    } catch (e: Throwable) {
        val msg = "couldn't load media:$id"
        log.e(msg, e)
        DbResult.Error<MediaDomain>(e, msg)
    }

    private fun fillAndMapEntity(media: Media): MediaDomain = mediaMapper.map(
        media,
        channelDatabaseRepository.loadChannelInternal(media.channel_id!!.toGUID()).data!!,
        imageDatabaseRepository.loadEntity(media.thumb_id?.toGUID()),
        imageDatabaseRepository.loadEntity(media.image_id?.toGUID()),
    )

    internal fun saveMediasInternal(
        domains: List<MediaDomain>,
        flat: Boolean
    ) = domains
        .map { saveInternal(it, flat) }
        .map { loadMediaInternal(it).data!! }

    private fun saveInternal(input: MediaDomain, flat: Boolean): GUID =
        input
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
            .let { toSaveDomain ->
                with(database.mediaEntityQueries) {
                    val platformCheck = try {
                        loadByPlatformId(toSaveDomain.platformId, toSaveDomain.platform).executeAsOne()
                    } catch (n: NullPointerException) {
                        null
                    }
                    // log.d("platformCheck:${platformCheck?.run{"media.id=${id}, media.platformId=${platform_id} media.duration=${duration}"}}")
                    if (toSaveDomain.id?.source == source) {
                        val mediaEntity = mediaMapper.map(toSaveDomain)
                        // manual check for platform-platformId duplication
                        // throw ConflictException if id is different for same platform-platformId
                        if (platformCheck != null && platformCheck.id != mediaEntity.id) {
                            throw ConflictException(
                                "conflicting media id for ${mediaEntity.platform}_${mediaEntity.platform_id}" +
                                        " existing: ${platformCheck.id}  thisid:${mediaEntity.id}"
                            )
                        }
                        log.d("update entity: ${mediaEntity.duration}")
                        update(mediaEntity)
                        loadById(toSaveDomain.id!!.id.value).executeAsOne().id.toGUID()
                    } else {
                        // fixme some logic to add here need to merge the entities or throw conflict
                        if (platformCheck != null) {
                            //throw ConflictException("media already exists: ${toSaveDomain.platform}_${toSaveDomain.platformId}")
                            return platformCheck.id.toGUID()
                        } else {
                            // log.e("create:${toSaveDomain.apply{"media.id=${id}, media.platformId=${platformId} media.duration=${duration}"}}")
                            guidCreator.create().toIdentifier(source)
                                .apply { create(mediaMapper.map(toSaveDomain.copy(id = this))) }
                                .id
                        }
                    }
                }
            }
}
