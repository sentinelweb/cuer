package uk.co.sentinelweb.cuer.remote.server

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import uk.co.sentinelweb.cuer.app.db.repository.file.FileInteractor
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseRemoteNodeList
import uk.co.sentinelweb.cuer.domain.ext.serialise

class RemotesRepository constructor(
    private val fileInteractor: FileInteractor,
    private val localNodeRepo: LocalRepository,
    private val coroutines: CoroutineContextProvider,
) {
    private var _remoteNodes: MutableList<RemoteNodeDomain> = mutableListOf()

    private val _updatesFlow: MutableStateFlow<List<RemoteNodeDomain>> = MutableStateFlow(emptyList())
    val updatesFlow: Flow<List<RemoteNodeDomain>> get() = _updatesFlow

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

        _updatesFlow.value = _remoteNodes
        return _remoteNodes
    }

    suspend fun addUpdateNode(node: RemoteNodeDomain) = updateRemotesMutex.withLock {
        val local = localNodeRepo.getLocalNode()
        if (node.id == local.id) return

        removeNodeInternal(node)
        _remoteNodes.add(node)
        saveAll()
        _updatesFlow.value = _remoteNodes
    }

    suspend fun removeNode(node: RemoteNodeDomain) = updateRemotesMutex.withLock {
        removeNodeInternal(node)
        saveAll()
        _updatesFlow.value = _remoteNodes
    }

    private fun removeNodeInternal(node: RemoteNodeDomain) {
        _remoteNodes
            .find { it.id == node.id }
            ?.also { _remoteNodes.remove(it) }
    }

    private fun saveAll() {
        fileInteractor.saveJson(_remoteNodes.serialise())
    }

    suspend fun setDisconnected() = updateRemotesMutex.withLock {
        _remoteNodes
            .map { it.copy(isConnected = false) }
            .also { _remoteNodes.clear() }
            .also { _remoteNodes.addAll(it) }
            .also { saveAll() }
            .also { _updatesFlow.value = _remoteNodes }
    }
}


