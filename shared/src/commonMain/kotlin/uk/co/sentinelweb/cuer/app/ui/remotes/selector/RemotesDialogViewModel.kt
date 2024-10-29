package uk.co.sentinelweb.cuer.app.ui.remotes.selector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesModelMapper
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogContract.Model
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.remote.RemotePlayerInteractor
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.locator

class RemotesDialogViewModel(
    private val state: RemotesDialogContract.State,
    private val repo: RemotesRepository,
    private val mapper: RemotesModelMapper,
    private val playerInteractor: RemotePlayerInteractor,
    private val coroutines: CoroutineContextProvider,
) {

    lateinit var listener: (RemoteNodeDomain, PlayerNodeDomain.Screen?) -> Unit

    private val _model = MutableStateFlow(Model.blank)
    val model: Flow<Model> = _model

    init {
        coroutines.mainScope.launch { map() }
    }

    fun setSelectNodeOnly() {
        state.isSelectNodeOnly = true
    }

    fun onNodeSelected(node: RemoteNodeDomain) {
        coroutines.mainScope.launch {
            if (state.isSelectNodeOnly) {
                listener(node, null)
            } else {
                remoteSelected(node)
            }
        }
    }

    private suspend fun remoteSelected(node: RemoteNodeDomain) = withContext(coroutines.IO) {
        playerInteractor.playerSessionStatus(node.locator())
            .data
            ?.screen
            ?.also { listener(node, it) }
            ?: run {
                state.selectedNodeConfig = playerInteractor.getPlayerConfig(node.locator()).data
                state.selectedNodeConfig?.apply {
                    if (this.screens.size == 1) {
                        listener(node, screens[0])
                    } else {
                        _model.value = Model(listOf(mapper.mapNodeAndScreen(node, this)))
                    }
                }
            }
    }

    fun onScreenSelected(node: RemoteNodeDomain, screen: PlayerNodeDomain.Screen) {
        listener(node, screen)
    }

    private fun map() {
        _model.value = Model(repo.remoteNodes.map { mapper.mapRemoteNode(it) })
    }

    fun resetState() {
        state.selectedNode = null
        state.selectedNodeConfig = null
        state.isSelectNodeOnly = false
    }
}
