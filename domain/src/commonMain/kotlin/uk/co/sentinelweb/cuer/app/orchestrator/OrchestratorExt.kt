package uk.co.sentinelweb.cuer.app.orchestrator

import uk.co.sentinelweb.cuer.app.db.repository.RepoResult
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.net.NetResult

inline fun <reified T> RepoResult<List<T>>.forceDatabaseListResultNotEmpty(msg: String): List<T> = this.let {
    (it.takeIf { it.isSuccessful }
        ?: throw DatabaseException(it))
        .takeIf { (it.data?.size ?: 0) > 0 }
        ?.data
        ?: throw DoesNotExistException(msg)
}

inline fun <reified T> NetResult<List<T>>.forceNetListResultNotEmpty(msg: String): List<T> = this.let {
    (it.takeIf { it.isSuccessful }
        ?: throw NetException(it))
        .takeIf { (it.data?.size ?: 0) > 0 }
        ?.data
        ?: throw DoesNotExistException(msg)
}

inline fun <reified T> RepoResult<List<T>>.allowDatabaseListResultEmpty(): List<T> = this.let {
    (it.takeIf { it.isSuccessful }
        ?: throw DatabaseException(it))
        .data
        ?: listOf()
}

inline fun <reified T> NetResult<List<T>>.allowNetListResultEmpty(): List<T> = this.let {
    (it.takeIf { it.isSuccessful }
        ?: throw NetException(it))
        .data
        ?: listOf()
}

inline fun <reified T> NetResult<T>.forceNetSuccessNotNull(msg: String): T = this.let {
    (it.takeIf { it.isSuccessful }
        ?: throw NetException(it))
        .data
        ?: throw DoesNotExistException(msg)
}

inline fun <reified T> RepoResult<T>.forceDatabaseSuccessNotNull(msg: String): T = this.let {
    (it.takeIf { it.isSuccessful }
        ?: throw DatabaseException(it))
        .data
        ?: throw DoesNotExistException(msg)
}

inline fun <reified T> RepoResult<T>.forceDatabaseSuccess(): T? = this.let {
    (it.takeIf { it.isSuccessful }
        ?: throw DatabaseException(it))
        .data
}

inline fun <reified T> NetResult<T>.forceNetSuccess(): T? = this.let {
    (it.takeIf { it.isSuccessful }
        ?: throw NetException(it)).data
}

inline fun <reified Id> Pair<Id, Source>.toIdentifier() = Identifier(first, second)

fun Long.toIdentifier(source: Source) = Identifier(this, source)
fun Long.toLocalIdentifier() = Identifier(this, Source.LOCAL)
fun Long.toMemoryIdentifier() = Identifier(this, Source.MEMORY)

inline fun <reified Id> Identifier<Id>.toPair() = Pair(id, source)
inline fun <reified Id> Identifier<*>.toPairType() = Pair(id, source)
fun Identifier<*>.flatOptions(emit: Boolean = true) = Options(source, flat = true, emit = emit)
fun Identifier<*>.deepOptions(emit: Boolean = true) = Options(source, flat = false, emit = emit)
fun Source.flatOptions(emit: Boolean = true) = Options(this, flat = true, emit = emit)
fun Source.deepOptions(emit: Boolean = true) = Options(this, flat = false, emit = emit)
