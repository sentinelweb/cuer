package uk.co.sentinelweb.cuer.hub.ui.home

import uk.co.sentinelweb.cuer.hub.ui.remotes.RemotesUiCoordinator
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator

class HomeUiCoordinator : UiCoordinator<HomeModel> {

    private lateinit var updater: (HomeModel) -> Unit
    val remotes: RemotesUiCoordinator = RemotesUiCoordinator()

    override fun create() {
        remotes.create()
    }

    override fun destroy() {
        remotes.destroy()
    }

    override fun observeModel(updater: (HomeModel) -> Unit) {
        this.updater = updater
    }
}