package uk.co.sentinelweb.cuer.app.orchestrator

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.db.repository.DbResult
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.toGUID
import uk.co.sentinelweb.cuer.domain.update.UpdateDomain
import uk.co.sentinelweb.cuer.net.NetResult
import kotlin.reflect.KClass

interface OrchestratorContract<Domain> {

    // to support generic orchestrator injection via koin
    enum class Inject { Playlist, PlaylistStats, PlaylistItem, Media, Channel }
    enum class Source { MEMORY, LOCAL, LOCAL_NETWORK, REMOTE, PLATFORM }

    val updates: Flow<Triple<Operation, Source, Domain>>

    suspend fun loadByPlatformId(platformId: String, options: Options): Domain?

    suspend fun loadByDomain(domain: Domain, options: Options): Domain?

    suspend fun loadById(id: GUID, options: Options): Domain?

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

    // todo sealed class
    sealed class Filter {
        object DefaultFilter : Filter()
        object AllFilter : Filter()
        data class IdListFilter(val ids: List<GUID>) : Filter()
        data class MediaIdListFilter(val ids: List<GUID>) : Filter()
        data class PlatformIdListFilter(
            val ids: List<String>,
            val platform: PlatformDomain = PlatformDomain.YOUTUBE
        ) : Filter()

        data class ChannelPlatformIdFilter(val platformId: String) : Filter()
        data class NewMediaFilter(val limit: Int) : Filter()
        data class RecentMediaFilter(val limit: Int) : Filter()
        data class StarredMediaFilter(val limit: Int) : Filter()
        data class LiveUpcomingMediaFilter(val limit: Int) : Filter()
        data class UnfinishedMediaFilter(val minPercent: Int, val maxPercent: Int, val limit: Int) : Filter()
        data class TitleFilter(val title: String) : Filter()
        data class SearchFilter(
            val text: String,
            val isWatched: Boolean,
            val isNew: Boolean,
            val isLive: Boolean,
            val playlistIds: List<GUID>?
        ) : Filter()

        data class PlaylistIdLFilter(val id: GUID) : Filter()
    }

    class InvalidOperationException(
        clazz: KClass<out OrchestratorContract<out Any>>,
        filter: Filter?,
        options: Options
    ) : UnsupportedOperationException("class = ${clazz.simpleName} filter = $filter options = $options")

    class NotImplementedException(msg: String? = null) : Exception(msg)
    class DoesNotExistException(msg: String? = null) : Exception(msg)
    class DatabaseException(result: DbResult<*>) : Exception((result as DbResult.Error<*>).msg, result.t)
    class NetException(result: NetResult<*>) : Exception((result as NetResult.Error<*>).msg, result.t)
    class MemoryException(msg: String, cause: Throwable? = null) : Exception(msg, cause)

    @Serializable
    // todo make a data class - no need to subclass
    data class Identifier<IdType>(
        val id: IdType,
        val source: Source,
    )

    companion object {
        val NO_PLAYLIST = Identifier("no-playlist".toGUID(), MEMORY)
        val EMPTY_ID = Identifier("empty-id".toGUID(), MEMORY)
    }
}

fun String.toGuidIdentifier(source: Source): Identifier<GUID> = Identifier(GUID(this), source)
fun GUID.toIdentifier(source: Source): Identifier<GUID> = Identifier(this, source)