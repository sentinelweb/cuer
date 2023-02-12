package uk.co.sentinelweb.cuer.app.ui.remotes

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore.ServerState
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore.ServerState.INITIAL
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.NodeDomain

class RemotesContract {

    interface MviStore : Store<MviStore.Intent, MviStore.State, MviStore.Label> {
        sealed class Intent {
            object Up : Intent()
            object ActionSettings : Intent()
            object ActionPasteAdd : Intent()
            object ActionSearch : Intent()
            object ActionHelp : Intent()
            object ActionStartServer : Intent()
            object ActionStopServer : Intent()
            object ActionPing : Intent()
            object ActionConfig : Intent()
        }

        sealed class Label {
            object Up : Label()
            object ActionSettings : Label()
            object ActionSearch : Label()
            object ActionHelp : Label()
            object ActionPasteAdd : Label()
            data class Message(val msg: String) : Label()

        }

        enum class ServerState {
            INITIAL, STARTED, STOPPED
        }

        data class State(
            val serverState: ServerState = INITIAL,
            val serverAddress: String? = null,
            val localNode: NodeDomain? = null,
            val remoteNodes: List<NodeDomain> = listOf(),
        )
    }

    interface View : MviView<View.Model, View.Event> {

        fun processLabel(label: MviStore.Label)

        data class Model(
            val title: String,
            val imageUrl: String?,
            val address: String?,
            val localNode: NodeModel?,
            val remoteNodes: List<NodeModel>,
            val serverState: ServerState = INITIAL,
        )

        data class NodeModel(
            val id: OrchestratorContract.Identifier<GUID>,
            val title: String,
            val address: String,
            val hostname: String,
            val device: String,
            val deviceType: NodeDomain.DeviceType
        )

        sealed class Event {
            //object OnSendPing : Event()
            object OnActionSettingsClicked : Event()
            object OnActionPasteAdd : Event()
            object OnActionSearchClicked : Event()
            object OnActionHelpClicked : Event()
            object OnUpClicked : Event()
            object OnActionStartServerClicked : Event()
            object OnActionStopServerClicked : Event()
            object OnActionPingClicked : Event()
            object OnActionConfigClicked : Event()
        }
    }
}