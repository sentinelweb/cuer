package uk.co.sentinelweb.cuer.app.ui.remotes.selector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesModelMapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository

class RemotesDialogViewModel(
    val repo: RemotesRepository,
    val mapper: RemotesModelMapper,
    val coroutines: CoroutineContextProvider,
) {

    lateinit var listener: (RemoteNodeDomain) -> Unit

    private val _model = MutableStateFlow(RemotesDialogContract.Model.blank)
    val model: Flow<RemotesDialogContract.Model> = _model

    init {
        coroutines.mainScope.launch {
            map()
        }
    }

    fun onSelected(node: RemoteNodeDomain) {
        listener(node)
    }

    private suspend fun map() {
        _model.value = RemotesDialogContract.Model(repo.loadAll().map { mapper.mapRemoteNode(it) })
    }
}
