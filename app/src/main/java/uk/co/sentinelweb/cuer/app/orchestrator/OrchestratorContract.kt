package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult
import uk.co.sentinelweb.cuer.net.NetResult
import kotlin.reflect.KClass

interface OrchestratorContract<Domain> {

    enum class Source { MEMORY, LOCAL, LOCAL_NETWORK, REMOTE, PLATFORM }

    val updates: Flow<Pair<Operation, Domain>>

    suspend fun load(platformId: String, options: Options): Domain?

    suspend fun load(domain: Domain, options: Options): Domain?

    suspend fun load(id: Long, options: Options): Domain?

    suspend fun loadList(filter: Filter, options: Options): List<Domain>

    suspend fun save(domain: Domain, options: Options): Domain

    suspend fun save(domains: List<Domain>, options: Options): List<Domain>

    suspend fun count(filter: Filter, options: Options): Int

    suspend fun delete(domain: Domain, options: Options): Boolean

    interface Filter

    open class Options constructor(
        val source: Source,
        val flat: Boolean = true,
        val emit: Boolean = true
    ) {
        companion object {
            val LOCAL_FLAT = Options(Source.LOCAL)
            val LOCAL_DEEP = Options(Source.LOCAL, flat = false)
        }
    }

    class DeleteOptions(source: Source) : Options(source, false)

    enum class Operation { FLAT, FULL, DELETE }

    class IdListFilter(val ids: List<Long>) : Filter
    class MediaIdListFilter(val ids: List<Long>) : Filter
    class PlatformIdListFilter(val ids: List<String>) : Filter
    class DefaultFilter() : Filter
    class AllFilter() : Filter

    class InvalidOperationException(clazz: KClass<out OrchestratorContract<out Any>>, filter: Filter?, options: Options) :
        java.lang.UnsupportedOperationException("class = ${clazz.simpleName} filter = $filter options = $options")

    class NotImplementedException(msg: String? = null) : Exception(msg)
    class DoesNotExistException(msg: String? = null) : Exception(msg)
    class DatabaseException(result: RepoResult<*>) : Exception((result as RepoResult.Error<*>).msg, result.t)
    class NetException(result: NetResult<*>) : Exception((result as NetResult.Error<*>).msg, result.t)
}