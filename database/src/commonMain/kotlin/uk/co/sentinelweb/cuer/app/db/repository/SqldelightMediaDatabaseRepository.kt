package uk.co.sentinelweb.cuer.app.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.db.Media
import uk.co.sentinelweb.cuer.app.db.mapper.MediaMapper
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FLAT
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class SqldelightMediaDatabaseRepository(
    private val database: Database,
    private val imageDatabaseRepository: SqldelightImageDatabaseRepository,
    private val channelDatabaseRepository: SqldelightChannelDatabaseRepository,
    private val mediaMapper: MediaMapper,
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

    private fun saveInternal(domain: MediaDomain, flat: Boolean): Long =
        domain
            .let { if (!flat) it.copy(channelData = channelDatabaseRepository.checkToSaveChannelInternal(it.channelData)) else it }
            .let {
                it.copy(thumbNail = it.thumbNail
                    ?.let { imageDatabaseRepository.checkToSaveImage(it) })
            }
            .let {
                it.copy(image = it.image
                    ?.let { imageDatabaseRepository.checkToSaveImage(it) })
            }
            .let {
                val channel = mediaMapper.map(it)
                if (channel.id > 0) {
                    database.mediaEntityQueries
                        .update(channel)
                    domain.id!!
                } else {
                    database.mediaEntityQueries
                        .create(channel)
                    database.channelEntityQueries
                        .getInsertId()
                        .executeAsOne()
                }
            }

    override suspend fun save(domains: List<MediaDomain>, flat: Boolean, emit: Boolean): RepoResult<List<MediaDomain>> =
        withContext(coProvider.IO) {
            database.mediaEntityQueries.transactionWithResult<RepoResult<List<MediaDomain>>> {
                try {
                    domains
                        .map { saveInternal(it, flat) }
                        .map { loadMediaInternal(it).data!! }
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
                                // todo make query to load all
                                .mapNotNull {
                                    database.mediaEntityQueries
                                        .loadByPlatformId(it, YOUTUBE)
                                        .executeAsOne()
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

    override suspend fun loadStatsList(filter: OrchestratorContract.Filter?): RepoResult<List<Nothing>> {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: OrchestratorContract.Filter?): RepoResult<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(domain: MediaDomain, emit: Boolean): RepoResult<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll(): RepoResult<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun update(
        update: UpdateDomain<MediaDomain>,
        flat: Boolean,
        emit: Boolean
    ): RepoResult<MediaDomain> {
        TODO("Not yet implemented")
    }

    internal suspend fun loadMedia(id: Long): RepoResult<MediaDomain> =
        withContext(coProvider.IO) {
            loadMediaInternal(id)
        }

    private fun loadMediaInternal(id: Long) = try {
        database.mediaEntityQueries
            .loadById(id)
            .executeAsOneOrNull()!!
            .let { media: Media -> fillAndMapEntity(media) }
            .let { media: MediaDomain -> RepoResult.Data(media) }
    } catch (e: Throwable) {
        val msg = "couldn't load $id"
        log.e(msg, e)
        RepoResult.Error<MediaDomain>(e, msg)
    }

    private fun fillAndMapEntity(media: Media): MediaDomain = mediaMapper.map(
        media,
        channelDatabaseRepository.loadChannelInternal(media.channel_id!!).data!!,
        imageDatabaseRepository.loadEntity(media.thumb_id),
        imageDatabaseRepository.loadEntity(media.image_id),
    )

}