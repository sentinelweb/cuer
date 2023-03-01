package uk.co.sentinelweb.cuer.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.db.repository.ImageDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.AllFilter
import uk.co.sentinelweb.cuer.app.orchestrator.toGuidIdentifier
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.database.entity.Image
import uk.co.sentinelweb.cuer.db.mapper.ImageMapper
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain

class SqldelightImageDatabaseRepository(
    private val database: Database,
    private val imageMapper: ImageMapper,
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper,
    private val guidCreator: GuidCreator,
    private val source: OrchestratorContract.Source,
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
            with(database.imageEntityQueries) {
                try {

                    if (domain.id?.source == source) {
                        val entity = imageMapper.map(domain)
                        update(entity)
                        RepoResult.Data(imageMapper.map(entity))
                    } else {
                        guidCreator.create().toIdentifier(source)
                            .let { domain.copy(id = it) }
                            .also { create(imageMapper.map(it)) }
                            .let { RepoResult.Data(it) }
                    }
                } catch (e: Exception) {
                    val msg = "couldn't save image"
                    log.e(msg, e)
                    RepoResult.Error<ImageDomain>(e, msg)
                }
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

    override suspend fun load(id: GUID, flat: Boolean): RepoResult<ImageDomain> =
        withContext(coProvider.IO) {
            try {
                database.imageEntityQueries
                    .load(id.value)
                    .executeAsOneOrNull()!!
                    .let { image: Image -> imageMapper.map(image) }
                    .let { image: ImageDomain -> RepoResult.Data(image) }
            } catch (e: Throwable) {
                val msg = "couldn't load image $id"
                log.e(msg, e)
                RepoResult.Error<ImageDomain>(e, msg)
            }
        }

    override suspend fun loadList(filter: Filter, flat: Boolean): RepoResult<List<ImageDomain>> {
        TODO("Not yet implemented")
    }

    override suspend fun loadStatsList(filter: Filter): RepoResult<List<Nothing>> {
        TODO("Not yet implemented")
    }

    override suspend fun count(filter: Filter): RepoResult<Int> = withContext(coProvider.IO) {
        try {
            when (filter) {
                is AllFilter -> RepoResult.Data(
                    database.imageEntityQueries.count().executeAsOne().toInt()
                )

                else -> throw IllegalArgumentException("$filter not implemented")
            }
        } catch (e: Exception) {
            val msg = "couldn't count images"
            log.e(msg, e)
            RepoResult.Error<Int>(e, msg)
        }
    }

    override suspend fun delete(domain: ImageDomain, emit: Boolean): RepoResult<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll(): RepoResult<Boolean> = withContext(coProvider.IO) {
        var result: RepoResult<Boolean> = RepoResult.Data(false)
        database.imageEntityQueries.transaction {
            result = try {
                database.imageEntityQueries
                    .deleteAll()
                RepoResult.Data(true)
            } catch (e: Throwable) {
                val msg = "couldn't deleteAll images"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        }
        result
    }

    override suspend fun update(
        update: UpdateDomain<ImageDomain>,
        flat: Boolean,
        emit: Boolean
    ): RepoResult<ImageDomain> {
        throw NotImplementedError("Not used")
    }

    internal fun loadEntity(id: GUID?): Image? =
        id?.let {
            database.imageEntityQueries
                .load(it.value)
                .executeAsOneOrNull()
        }

    internal fun checkToSaveImage(domain: ImageDomain): ImageDomain =
        if (domain.id?.source == source) {
            imageMapper.map(domain).apply {
                database.imageEntityQueries.update(this)
            }
        } else {
            with(database.imageEntityQueries) {
                loadByUrl(domain.url)
                    .executeAsOneOrNull()
                    ?.let { imageMapper.map(domain.copy(id = it.id.toGuidIdentifier(source))) }
                    ?.also { database.imageEntityQueries.update(it) }
                    ?: let {
                        guidCreator.create().toIdentifier(source)
                            .let { imageMapper.map(domain.copy(id = it)) }
                            .also { create(it) }
                    }
            }
        }.let { imageMapper.map(it) }

    internal suspend fun delete(id: GUID?): RepoResult<Boolean> = withContext(coProvider.IO) {
        id?.let {
            try {
                database.imageEntityQueries.delete(id.value)
                RepoResult.Data(true)
            } catch (e: Throwable) {
                val msg = "couldn't delete image: $id"
                log.e(msg, e)
                RepoResult.Error<Boolean>(e, msg)
            }
        } ?: RepoResult.Data(false)
    }
}