package uk.co.sentinelweb.cuer.remote.server

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import uk.co.sentinelweb.cuer.app.db.repository.file.JsonFileInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseRemoteNodeList
import uk.co.sentinelweb.cuer.domain.ext.serialise

class RemotesRepository constructor(
    private val jsonFileInteractor: JsonFileInteractor,
    private val localNodeRepo: LocalRepository,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper
) {
    init {
        log.tag(this)
    }

    private var _remoteNodes: MutableList<RemoteNodeDomain> = mutableListOf()
    val remoteNodes: List<RemoteNodeDomain>
        get() = _remoteNodes

    private val _updatesFlow: MutableStateFlow<List<RemoteNodeDomain>> = MutableStateFlow(emptyList())

    val updatesFlow: Flow<List<RemoteNodeDomain>>
        get() = _updatesFlow

    private val updateRemotesMutex = Mutex()

    init {
        coroutines.computationScope.launch { loadAll() }
    }

    fun getById(guid: GUID): RemoteNodeDomain? =
        _remoteNodes.find { it.id?.id == guid }
    fun getByLocator(locator: OrchestratorContract.Identifier.Locator): RemoteNodeDomain? =
        _remoteNodes.find { it.locator() == locator }

    suspend fun getByName(name: String): RemoteNodeDomain? = updateRemotesMutex.withLock {
        _remoteNodes
            .find { it.hostname == name }
    }

    suspend fun loadAll(): List<RemoteNodeDomain> = updateRemotesMutex.withLock {
        _remoteNodes.clear()
        jsonFileInteractor.loadJson()
            ?.takeIf { it.isNotEmpty() }
            ?.let { deserialiseRemoteNodeList(it) }
            ?.let { it.map { it.copy(isAvailable = false) } }
            ?.let { _remoteNodes.addAll(it) }

        _updatesFlow.emit(_remoteNodes.toList())
        return _remoteNodes
    }

    suspend fun addUpdateNode(node: RemoteNodeDomain) = updateRemotesMutex.withLock {
        val local = localNodeRepo.localNode
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

    private fun summarise(node: RemoteNodeDomain) =
        "node: ${node.isAvailable} ${node.hostname} ${node.ipAddress} ${node.id}"

    suspend fun removeNode(node: RemoteNodeDomain) = updateRemotesMutex.withLock {
        _remoteNodes
            .find { it.id == node.id }
            ?.also { _remoteNodes.remove(it) }
        saveAll()
        _updatesFlow.emit(_remoteNodes.toList())
    }

    suspend fun setUnAvailable() = updateRemotesMutex.withLock {
        _remoteNodes
            .map { it.copy(isAvailable = false) }
            .also { _remoteNodes.clear() }
            .also { _remoteNodes.addAll(it) }
            .also { saveAll() }
            .also { _updatesFlow.emit(_remoteNodes.toList()) }
    }

    private fun saveAll() {
        jsonFileInteractor.saveJson(_remoteNodes.serialise())
    }

    suspend fun updateAddress(remote: RemoteNodeDomain, newAddress: String) {
        val splitPair = newAddress.split(":")
            .takeIf { it.size == 2 }
            ?.let { it[0] to it[1].toInt() }

        _remoteNodes
            .takeIf { splitPair != null }
            ?.find { it.id == remote.id }
            ?.copy( ipAddress = splitPair!!.first, port = splitPair.second)
            ?.also {addUpdateNode(it)}

    }
}


