package uk.co.sentinelweb.cuer.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.db.repository.ConflictException
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult
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
import uk.co.sentinelweb.cuer.domain.creator.GUIDCreator
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
    private val guidCreator: GUIDCreator,
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

    override suspend fun save(domain: MediaDomain, flat: Boolean, emit: Boolean): RepoResult<MediaDomain> =
        withContext(coProvider.IO) {
            try {
                val id = saveInternal(domain, flat)
                val loadResult = load(id = id, flat)
                if (loadResult.isSuccessful)
                    RepoResult.Data(loadResult.data)
                        .also {
                            if (emit) it.data
                                ?.also { _updatesFlow.emit((if (flat) FLAT else FULL) to it) }
                        }
                else loadResult
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

    override suspend fun load(id: GUID, flat: Boolean): RepoResult<MediaDomain> = loadMedia(id)

    override suspend fun loadList(filter: Filter, flat: Boolean): RepoResult<List<MediaDomain>> =
        withContext(coProvider.IO) {
            database.mediaEntityQueries.transactionWithResult<RepoResult<List<MediaDomain>>> {
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
                            .let { RepoResult.Data.dataOrEmpty(it) }
                    }
                } catch (e: Exception) {
                    val msg = "couldn't load medias"
                    log.e(msg, e)
                    RepoResult.Error<List<MediaDomain>>(e, msg)
                }
            }
        }

    override suspend fun loadStatsList(filter: Filter): RepoResult<List<Nothing>> {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: Filter): RepoResult<Int> = withContext(coProvider.IO) {
        try {
            when (filter) {
                is AllFilter -> RepoResult.Data(database.mediaEntityQueries.count().executeAsOne().toInt())
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
                ?.let { database.mediaEntityQueries.delete(it.id.value) }
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

    private suspend fun loadMedia(id: GUID): RepoResult<MediaDomain> =
        withContext(coProvider.IO) {
            loadMediaInternal(id)
        }

    internal fun loadMediaInternal(id: GUID) = try {
        database.mediaEntityQueries
            .loadById(id.value)
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
            .let { domain ->

                with(database.mediaEntityQueries) {
                    val platformCheck = try {
                        loadByPlatformId(domain.platformId, domain.platform).executeAsOne()
                    } catch (n: NullPointerException) {
                        null
                    }
                    if (domain.id != null) {
                        val mediaEntity = mediaMapper.map(domain)
                        // manual check for platform-platformId duplication
                        // throw ConflictException if id is different for same platform-platformId
                        if (platformCheck != null && platformCheck.id != mediaEntity.id) {
                            throw ConflictException(
                                "conflicting media id for ${mediaEntity.platform}_${mediaEntity.platform_id}" +
                                        " existing: ${platformCheck.id}  thisid:${mediaEntity.id}"
                            )
                        }
                        update(mediaEntity)
                        loadById(domain.id!!.id.value).executeAsOne().id.toGUID()
                    } else {
                        if (platformCheck != null) {
                            throw ConflictException("media already exists: ${domain.platform}_${domain.platformId}")
                        }
                        guidCreator.create().toIdentifier(source)
                            .apply { create(mediaMapper.map(domain.copy(id = this))) }
                            .id
//                        create(mediaEntity)
//                        getInsertId().executeAsOne()
                    }
                }
            }
}
