package uk.co.sentinelweb.cuer.remote.server

import uk.co.sentinelweb.cuer.app.db.repository.file.FileInteractor
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseRemoteNodeList
import uk.co.sentinelweb.cuer.domain.ext.serialise

class RemotesRepository constructor(
    private val fileInteractor: FileInteractor,
    private val localNodeRepo: LocalRepository,
) {
    private var _remoteNodes: MutableList<RemoteNodeDomain> = mutableListOf()

    val remoteNodes: List<RemoteNodeDomain>
        get() = _remoteNodes

    fun loadAll(): List<RemoteNodeDomain> {
        _remoteNodes.clear()
        fileInteractor.loadJson()
            ?.takeIf { it.isNotEmpty() }
            ?.let { _remoteNodes.addAll(deserialiseRemoteNodeList(it)) }
        return remoteNodes
    }

    fun save(list: List<RemoteNodeDomain>) {
        _remoteNodes.clear()
        _remoteNodes.addAll(list)
        saveNodeList(list)
    }

    private fun saveNodeList(list: List<RemoteNodeDomain>) {
        fileInteractor.saveJson(list.serialise())
    }

    fun addUpdateNode(node: RemoteNodeDomain) {
        val local = localNodeRepo.getLocalNode()
        if (node.ipAddress == local.ipAddress && node.port == local.port) return

        removeNodeInternal(node)
        _remoteNodes.add(node)
    }

    fun removeNode(node: RemoteNodeDomain) {
        removeNodeInternal(node)
    }

    private fun removeNodeInternal(node: RemoteNodeDomain) {
        _remoteNodes
            .find { it.ipAddress == node.ipAddress && it.port == node.port }
            ?.also { _remoteNodes.remove(it) }
    }
}


