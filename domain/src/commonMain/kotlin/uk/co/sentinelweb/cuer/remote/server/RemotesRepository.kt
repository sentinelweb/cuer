package uk.co.sentinelweb.cuer.remote.server

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import uk.co.sentinelweb.cuer.app.db.repository.file.FileInteractor
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseRemoteNodeList

class RemotesRepository constructor(
    private val fileInteractor: FileInteractor,
    private val localNodeRepo: LocalRepository,
) {
    private var _remoteNodes: MutableList<RemoteNodeDomain> = mutableListOf()

    //var updatesCallback: ((List<RemoteNodeDomain>) -> Unit)? = null
    private val _updatesFlow: MutableStateFlow<List<RemoteNodeDomain>> = MutableStateFlow(emptyList())
    val updatesFlow: Flow<List<RemoteNodeDomain>> get() = _updatesFlow
    val remoteNodes: List<RemoteNodeDomain>
        get() = _remoteNodes

    fun loadAll(): List<RemoteNodeDomain> {
        _remoteNodes.clear()
        fileInteractor.loadJson()
            ?.takeIf { it.isNotEmpty() }
            ?.let { _remoteNodes.addAll(deserialiseRemoteNodeList(it)) }

        //updatesCallback?.invoke(_remoteNodes)
        _updatesFlow.value = _remoteNodes
        return _remoteNodes
    }

    fun addUpdateNode(node: RemoteNodeDomain) {
        val local = localNodeRepo.getLocalNode()
        if (node.id == local.id) return

        removeNodeInternal(node)
        _remoteNodes.add(node)
        //updatesCallback?.invoke(_remoteNodes)
        _updatesFlow.value = _remoteNodes
    }

    fun removeNode(node: RemoteNodeDomain) {
        removeNodeInternal(node)
        //updatesCallback?.invoke(_remoteNodes)
        _updatesFlow.value = _remoteNodes
    }

    private fun removeNodeInternal(node: RemoteNodeDomain) {
        _remoteNodes
            .find { it.id == node.id }
            ?.also { _remoteNodes.remove(it) }
    }
}


