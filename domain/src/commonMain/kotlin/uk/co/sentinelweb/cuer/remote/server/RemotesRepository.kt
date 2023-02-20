package uk.co.sentinelweb.cuer.remote.server

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import uk.co.sentinelweb.cuer.app.db.repository.file.FileInteractor
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseRemoteNodeList
import uk.co.sentinelweb.cuer.domain.ext.serialise

class RemotesRepository constructor(
    private val fileInteractor: FileInteractor,
    private val localNodeRepo: LocalRepository,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper
) {
    init {
        log.tag(this)
    }

    private var _remoteNodes: MutableList<RemoteNodeDomain> = mutableListOf()

    private val _updatesFlow: MutableStateFlow<List<RemoteNodeDomain>> = MutableStateFlow(emptyList())

    val updatesFlow: Flow<List<RemoteNodeDomain>>
        get() = _updatesFlow

    private val updateRemotesMutex = Mutex()

    init {
        coroutines.mainScope.launch { loadAll() }
    }

    suspend fun loadAll(): List<RemoteNodeDomain> = updateRemotesMutex.withLock {
        _remoteNodes.clear()
        fileInteractor.loadJson()
            ?.takeIf { it.isNotEmpty() }
            ?.let { deserialiseRemoteNodeList(it) }
            //?.let { it.map { it.copy(isConnected = false) } }
            ?.let { _remoteNodes.addAll(it) }

        _updatesFlow.emit(_remoteNodes.toList())
        return _remoteNodes
    }

    suspend fun addUpdateNode(node: RemoteNodeDomain) = updateRemotesMutex.withLock {
        val local = localNodeRepo.getLocalNode()
//        log.d("addUpdateNode: input isLocal: ${node.id == local.id} ${summarise(node)} ")
        if (node.id == local.id) return

        _remoteNodes
            .indexOfFirst { it.id == node.id }
            .takeIf { it >= 0 }
            ?.also { index -> _remoteNodes[index] = node }
            ?: _remoteNodes.add(node)

//        _remoteNodes.forEach { rn ->
//            log.d("addUpdateNode: node updated: ${summarise(rn)}")
//        }

        saveAll()
        _updatesFlow.value = _remoteNodes.toList()
    }

    private fun summarise(node: RemoteNodeDomain) = "node: ${node.isConnected} ${node.hostname} ${node.ipAddress} ${node.id}"

    suspend fun removeNode(node: RemoteNodeDomain) = updateRemotesMutex.withLock {
        _remoteNodes
            .find { it.id == node.id }
            ?.also { _remoteNodes.remove(it) }
        saveAll()
        _updatesFlow.emit(_remoteNodes.toList())
    }

    suspend fun setDisconnected() = updateRemotesMutex.withLock {
        _remoteNodes
            .map { it.copy(isConnected = false) }
            .also { _remoteNodes.clear() }
            .also { _remoteNodes.addAll(it) }
            .also { saveAll() }
            .also { _updatesFlow.emit(_remoteNodes.toList()) }
    }

    private fun saveAll() {
        fileInteractor.saveJson(_remoteNodes.serialise())
    }
}


