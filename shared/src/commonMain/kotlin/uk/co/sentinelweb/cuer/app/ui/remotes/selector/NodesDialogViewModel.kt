package uk.co.sentinelweb.cuer.app.ui.remotes.selector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.NodesDialogContract.Model
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.remote.RemotePlayerInteractor
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.locator

class NodesDialogViewModel(
    private val state: NodesDialogContract.State,
    private val remotesRepository: RemotesRepository,
    private val mapper: NodesDialogModelMapper,
    private val playerInteractor: RemotePlayerInteractor,
    private val coroutines: CoroutineContextProvider,
    private val localRepository: LocalRepository,
) {

    lateinit var listener: (NodeDomain, PlayerNodeDomain.Screen?) -> Unit

    private val _model = MutableStateFlow(Model.blank)
    val model: Flow<Model> = _model

    init {
        coroutines.mainScope.launch { map() }
    }

    fun setSelectNodeOnly() {
        state.isSelectNodeOnly = true
    }

    fun onNodeSelected(node: NodeDomain) {
        coroutines.mainScope.launch {
            if (state.isSelectNodeOnly) {
                listener(node, null)
            } else {
                if (node.locator() == localRepository.localNode.locator()) {
                    localSelected(node)
                } else {
                    remoteSelected(node)
                }
            }
        }
    }

    private suspend fun remoteSelected(node: NodeDomain) = withContext(coroutines.IO) {
        playerInteractor.playerSessionStatus(node.locator())
            .data
            ?.screen
            ?.also { listener(node, it) }
            ?: run {
                if (node is RemoteNodeDomain) {
                    state.selectedNodeConfig = playerInteractor.getPlayerConfig(node.locator()).data
                    state.selectedNodeConfig?.apply {
                        if (this.screens.size == 1) {
                            listener(node, screens[0])
                        } else {
                            _model.value = Model(listOf(mapper.mapNodeAndScreen(node, this)))
                        }
                    }
                } else {
                    listener(node, null)
                }
            }
    }

    private fun localSelected(node: NodeDomain) {
        // fixme get local node screens and display to user
        listener(node, null)
    }

    fun onScreenSelected(node: NodeDomain, screen: PlayerNodeDomain.Screen) {
        listener(node, screen)
    }

    private fun map() {
        _model.value = Model(
            listOf(localRepository.localNode)
                .plus(remotesRepository.remoteNodes)
                .map { mapper.mapNode(it) }
        )
    }

    fun resetState() {
        state.selectedNode = null
        state.selectedNodeConfig = null
        state.isSelectNodeOnly = false
    }
}
