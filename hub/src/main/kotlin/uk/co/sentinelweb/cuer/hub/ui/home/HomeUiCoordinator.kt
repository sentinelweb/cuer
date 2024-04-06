package uk.co.sentinelweb.cuer.hub.ui.home

import kotlinx.coroutines.flow.MutableStateFlow
import uk.co.sentinelweb.cuer.hub.ui.remotes.RemotesUiCoordinator
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator

class HomeUiCoordinator : UiCoordinator<HomeModel> {

    val remotes: RemotesUiCoordinator = RemotesUiCoordinator()
    override var modelObservable = MutableStateFlow(HomeModel(0))
        private set

    override fun create() {
        remotes.create()
    }

    override fun destroy() {
        remotes.destroy()
    }
}