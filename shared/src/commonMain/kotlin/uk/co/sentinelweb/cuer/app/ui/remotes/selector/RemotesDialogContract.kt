package uk.co.sentinelweb.cuer.app.ui.remotes.selector

import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.RemoteNodeModel
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain

interface RemotesDialogContract {
    interface Launcher {
        fun launchRemotesDialog(selected: (RemoteNodeDomain) -> Unit)

        fun hideRemotesDialog()
    }

    data class Model(
        val remotes: List<RemoteNodeModel>
    ) {
        companion object {
            val blank = Model(listOf())
        }
    }
}