package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.domain.update.UpdateObject
import uk.co.sentinelweb.cuer.net.NetResult
import kotlin.reflect.KClass

interface OrchestratorContract<Domain> {

    enum class Source { MEMORY, LOCAL, LOCAL_NETWORK, REMOTE, PLATFORM }

    val updates: Flow<Triple<Operation, Source, Domain>>

    suspend fun load(platformId: String, options: Options): Domain?

    suspend fun load(domain: Domain, options: Options): Domain?

    suspend fun load(id: Long, options: Options): Domain?

    suspend fun loadList(filter: Filter, options: Options): List<Domain>

    suspend fun save(domain: Domain, options: Options): Domain

    suspend fun save(domains: List<Domain>, options: Options): List<Domain>

    suspend fun count(filter: Filter, options: Options): Int

    suspend fun delete(domain: Domain, options: Options): Boolean

    suspend fun update(update: UpdateObject<Domain>, options: Options): Domain?

    interface Filter

    data class Options constructor(
        val source: Source,
        val flat: Boolean = true,
        val emit: Boolean = true
    )

    enum class Operation { FLAT, FULL, DELETE }

    class IdListFilter(val ids: List<Long>) : Filter
    class MediaIdListFilter(val ids: List<Long>) : Filter
    class PlatformIdListFilter(val ids: List<String>) : Filter
    class DefaultFilter() : Filter
    class AllFilter() : Filter
    class ChannelPlatformIdFilter(val platformId: String) : Filter
    class NewMediaFilter() : Filter
    class RecentMediaFilter() : Filter

    class InvalidOperationException(clazz: KClass<out OrchestratorContract<out Any>>, filter: Filter?, options: Options) :
        java.lang.UnsupportedOperationException("class = ${clazz.simpleName} filter = $filter options = $options")

    class NotImplementedException(msg: String? = null) : Exception(msg)
    class DoesNotExistException(msg: String? = null) : Exception(msg)
    class DatabaseException(result: RepoResult<*>) : Exception((result as RepoResult.Error<*>).msg, result.t)
    class NetException(result: NetResult<*>) : Exception((result as NetResult.Error<*>).msg, result.t)
    class MemoryException(msg: String, cause: Throwable? = null) : Exception(msg, cause)

    // todo make this Identifier serialzable and make a sealed class for ID add ObjectType
//    sealed class Id {
//        class IdLong(id:Long)
//        class IdString(id:String)
//    }
    // @Serializable
    open class Identifier<IdType>(
        open val id: IdType,
        val source: Source
        // todo val type:ObjectType
    ) {

        override fun equals(other: Any?): Boolean {
            return when (other) {
                is Identifier<*> -> this.id == other.id && this.source == other.source
                else -> super.equals(other)
            }
        }

        override fun hashCode(): Int {
            var result = id?.hashCode() ?: 0
            result = 31 * result + source.hashCode()
            return result
        }
    }

    data class LocalIdentifier(override val id: Long) : Identifier<Long>(id, LOCAL) {
        override fun equals(other: Any?): Boolean = super.equals(other)
        override fun hashCode(): Int = super.hashCode()
    }

    data class MemoryIdentifier(override val id: Long) : Identifier<Long>(id, Source.MEMORY) {
        override fun equals(other: Any?): Boolean = super.equals(other)
        override fun hashCode(): Int = super.hashCode()
    }

    companion object {
        val NO_PLAYLIST = LocalIdentifier(-1L)
    }
}