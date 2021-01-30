package uk.co.sentinelweb.cuer.app.orchestrator

import uk.co.sentinelweb.cuer.app.db.repository.RepoResult
import uk.co.sentinelweb.cuer.net.NetResult

inline fun <reified T> RepoResult<List<T>>.forceDatabaseListResultNotEmpty(msg: String) = this.let {
    (it.takeIf { it.isSuccessful }
        ?: throw OrchestratorContract.DatabaseException(it))
        .takeIf { (it.data?.size ?: 0) > 0 }
        ?.data
        ?: throw OrchestratorContract.DoesNotExistException(msg)
}

inline fun <reified T> NetResult<List<T>>.forceNetListResultNotEmpty(msg: String) = this.let {
    (it.takeIf { it.isSuccessful }
        ?: throw OrchestratorContract.NetException(it))
        .takeIf { (it.data?.size ?: 0) > 0 }
        ?.data
        ?: throw OrchestratorContract.DoesNotExistException(msg)
}

inline fun <reified T> NetResult<T>.forceNetResultNotNull(msg: String) = this.let {
    (it.takeIf { it.isSuccessful }
        ?: throw OrchestratorContract.NetException(it))
        .data
        ?: throw OrchestratorContract.DoesNotExistException(msg)
}

inline fun <reified T> RepoResult<T>.forceDatabaseSuccess(msg: String) = this.let {
    (it.takeIf { it.isSuccessful }
        ?.data)
        ?: throw OrchestratorContract.DatabaseException(it)
}

inline fun <reified T> RepoResult<T>.allowDatabaseFail() = this.takeIf { it.isSuccessful }?.data

inline fun <reified T> NetResult<T>.forceNetSuccess(msg: String) = this.let {
    (it.takeIf { it.isSuccessful }
        ?.data)
        ?: throw OrchestratorContract.NetException(it)
}

inline fun <reified T> NetResult<T>.allowNetFail() = this.takeIf { it.isSuccessful }?.data