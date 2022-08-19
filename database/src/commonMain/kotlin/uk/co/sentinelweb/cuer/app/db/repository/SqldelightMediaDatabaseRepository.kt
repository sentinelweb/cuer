package uk.co.sentinelweb.cuer.app.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.db.Media
import uk.co.sentinelweb.cuer.app.db.mapper.MediaMapper
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FLAT
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
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
                domain
                    .let { if (!flat) it.copy(channelData = channelDatabaseRepository.checkToSaveChannel(it.channelData)) else it }
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

    override suspend fun save(domains: List<MediaDomain>, flat: Boolean, emit: Boolean): RepoResult<List<MediaDomain>> {
        TODO("Not yet implemented")
    }

    override suspend fun load(id: Long, flat: Boolean): RepoResult<MediaDomain> = loadMedia(id) // todo flat

    override suspend fun loadList(filter: OrchestratorContract.Filter?, flat: Boolean): RepoResult<List<MediaDomain>> {
        TODO("Not yet implemented")
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
            .let { media: Media ->
                mediaMapper.map(
                    media,
                    channelDatabaseRepository.loadChannelInternal(media.channel_id!!).data!!,
                    imageDatabaseRepository.loadEntity(media.thumb_id),
                    imageDatabaseRepository.loadEntity(media.image_id),
                )
            }
            .let { media: MediaDomain -> RepoResult.Data(media) }
    } catch (e: Throwable) {
        val msg = "couldn't load $id"
        log.e(msg, e)
        RepoResult.Error<MediaDomain>(e, msg)
    }

}