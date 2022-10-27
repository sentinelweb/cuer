package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain
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

    suspend fun update(update: UpdateDomain<Domain>, options: Options): Domain?

    data class Options constructor(
        val source: Source,
        val flat: Boolean = true,
        val emit: Boolean = true,
    )

    enum class Operation { FLAT, FULL, DELETE }

    interface Filter
    data class IdListFilter(val ids: List<Long>) : Filter
    data class MediaIdListFilter(val ids: List<Long>) : Filter
    data class PlatformIdListFilter(
        val ids: List<String>,
        val platform: PlatformDomain = PlatformDomain.YOUTUBE
    ) : Filter

    class DefaultFilter() : Filter
    class AllFilter() : Filter
    data class ChannelPlatformIdFilter(val platformId: String) : Filter
    data class NewMediaFilter(val limit: Int) : Filter
    data class RecentMediaFilter(val limit: Int) : Filter
    data class StarredMediaFilter(val limit: Int) : Filter
    data class UnfinishedMediaFilter(val minPercent: Int, val maxPercent: Int, val limit: Int) : Filter
    data class TitleFilter(val title: String) : Filter
    data class SearchFilter(
        val text: String,
        val isWatched: Boolean,
        val isNew: Boolean,
        val isLive: Boolean,
        val playlistIds: List<Long>?
    ) : Filter

    data class PlaylistIdLFilter(val id: Long) : Filter

    class InvalidOperationException(
        clazz: KClass<out OrchestratorContract<out Any>>,
        filter: Filter?,
        options: Options
    ) :
        UnsupportedOperationException("class = ${clazz.simpleName} filter = $filter options = $options")

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
    // todo make a data class - no need to subclass
    open class Identifier<IdType>(
        open val id: IdType,
        val source: Source,
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

        override fun toString(): String = "${this::class.simpleName}(id=$id, source=$source)"
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