package uk.co.sentinelweb.cuer.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.database.entity.Image
import uk.co.sentinelweb.cuer.app.db.repository.ImageDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult
import uk.co.sentinelweb.cuer.db.mapper.ImageMapper
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class SqldelightImageDatabaseRepository(
    private val database: Database,
    private val imageMapper: ImageMapper,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper,
) : ImageDatabaseRepository {
    init {
        log.tag(this)
    }

    override val updates: Flow<Pair<OrchestratorContract.Operation, ImageDomain>>
        get() = throw NotImplementedError("Not used")
    override val stats: Flow<Pair<OrchestratorContract.Operation, Nothing>>
        get() = throw NotImplementedError("Not used")

    override suspend fun save(domain: ImageDomain, flat: Boolean, emit: Boolean): RepoResult<ImageDomain> =
        withContext(coProvider.IO) {
            try {
                val entity = imageMapper.map(domain)
                if (domain.id != null) {
                    database.imageEntityQueries.update(entity)
                    RepoResult.Data(imageMapper.map(entity))
                } else {
                    database.imageEntityQueries.create(entity)
                    val insertId = database.imageEntityQueries.getInsertId().executeAsOne()
                    RepoResult.Data(imageMapper.map(entity.copy(id = insertId)))
                }
            } catch (e: Exception) {
                val msg = "couldn't save image"
                log.e(msg, e)
                RepoResult.Error<ImageDomain>(e, msg)
            }
        }


    override suspend fun save(domains: List<ImageDomain>, flat: Boolean, emit: Boolean): RepoResult<List<ImageDomain>> =
        withContext(coProvider.IO) {
            database.imageEntityQueries.transactionWithResult {
                try {
                    domains
                        .map { checkToSaveImage(it) }
                        .let { RepoResult.Data(it) }
                } catch (e: Exception) {
                    val msg = "couldn't save images"
                    log.e(msg, e)
                    RepoResult.Error<List<ImageDomain>>(e, msg)
                }
            }
        }

    override suspend fun load(id: Long, flat: Boolean): RepoResult<ImageDomain> =
        withContext(coProvider.IO) {
            try {
                database.imageEntityQueries
                    .load(id)
                    .executeAsOneOrNull()!!
                    .let { image: Image -> imageMapper.map(image) }
                    .let { image: ImageDomain -> RepoResult.Data(image) }
            } catch (e: Throwable) {
                val msg = "couldn't load image $id"
                log.e(msg, e)
                RepoResult.Error<ImageDomain>(e, msg)
            }
        }

    override suspend fun loadList(filter: OrchestratorContract.Filter?, flat: Boolean): RepoResult<List<ImageDomain>> {
        TODO("Not yet implemented")
    }

    override suspend fun loadStatsList(filter: OrchestratorContract.Filter?): RepoResult<List<Nothing>> {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: OrchestratorContract.Filter?): RepoResult<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(domain: ImageDomain, emit: Boolean): RepoResult<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll(): RepoResult<Boolean> = withContext(coProvider.IO) {
        database.imageEntityQueries.transactionWithResult {
            try {
                database.imageEntityQueries.deleteAll()
                RepoResult.Data(true)
            } catch (e: Throwable) {
                val msg = "couldn't deleteAll images"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }
    }

    override suspend fun update(
        update: UpdateDomain<ImageDomain>,
        flat: Boolean,
        emit: Boolean
    ): RepoResult<ImageDomain> {
        throw NotImplementedError("Not used")
    }

    internal fun loadEntity(id: Long?): Image? =
        id?.let {
            database.imageEntityQueries
                .load(it)
                .executeAsOneOrNull()
        }

    internal fun checkToSaveImage(domain: ImageDomain): ImageDomain =
        domain
            .let { imageMapper.map(it) }
            .let { imageEntity ->
                if (imageEntity.id > 0) {
                    database.imageEntityQueries.update(imageEntity)
                    imageEntity
                } else {
                    database.imageEntityQueries
                        .loadByUrl(imageEntity.url)
                        .executeAsOneOrNull()
                        ?.let { imageEntity.copy(id = it.id) }
                        ?.also { database.imageEntityQueries.update(it) }
                        ?: let {
                            database.imageEntityQueries
                                .create(imageEntity)
                                .let { imageEntity.copy(id = database.imageEntityQueries.getInsertId().executeAsOne()) }
                        }
                }
            }.let { imageMapper.map(it) }

    internal suspend fun delete(id: Long?): RepoResult<Boolean> = withContext(coProvider.IO) {
        id?.let {
            try {
                database.imageEntityQueries.delete(it)
                RepoResult.Data(true)
            } catch (e: Throwable) {
                val msg = "couldn't delete image: $id"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        } ?: RepoResult.Data(false)
    }
}