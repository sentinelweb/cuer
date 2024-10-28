package uk.co.sentinelweb.cuer.app.ui.local

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import kotlinx.coroutines.flow.StateFlow
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Model
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain.AuthConfig
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.remote.server.ServerState
import uk.co.sentinelweb.cuer.remote.server.ServerState.INITIAL
import uk.co.sentinelweb.cuer.remote.server.ServerState.STOPPED

class LocalContract {

    interface MviStore : Store<MviStore.Intent, MviStore.State, MviStore.Label> {
        sealed class Intent {
            object Up : Intent()
            data class ActionSave(val updated: LocalNodeDomain) : Intent()
        }

        sealed class Label {
            object Up : Label()
            data class Message(val msg: String) : Label()
            object Saved : Label()

        }

        data class State(
            val serverState: ServerState = INITIAL,
            val serverAddress: String? = null,
            val localNode: LocalNodeDomain = DUMMY_LOCAL_NODE,
            val wifiState: WifiStateProvider.WifiState
        )
    }

    interface View : MviView<View.Model, View.Event> {

        val modelObservable: StateFlow<Model>

        fun processLabel(label: MviStore.Label)

        data class Model(
            val title: String,
            val imageUrl: String?,
            val localNodeDomain: LocalNodeDomain,
            val address: String?,
            val serverState: ServerState,
            val wifiState: WifiStateProvider.WifiState
        ) {
            companion object {
                val Initial = Model(
                    title = "",
                    imageUrl = "",
                    localNodeDomain = LocalNodeDomain(id = null, ipAddress = "", port = 0),
                    address = "",
                    serverState = STOPPED,
                    wifiState = WifiStateProvider.WifiState(),
                )

            }
        }


        sealed class Event {
            object OnUpClicked : Event()
            data class OnActionSaveClicked(val updated: LocalNodeDomain) : Event()
        }
    }

    companion object {
        val DUMMY_LOCAL_NODE = LocalNodeDomain(
            id = null,
            ipAddress = "",
            port = 0,
            hostname = "dummy",
            device = "",
            deviceType = NodeDomain.DeviceType.OTHER,
            authConfig = AuthConfig.Open
        )
    }
}
