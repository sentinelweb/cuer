package uk.co.sentinelweb.cuer.app.ui.local

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.remote.server.ServerState
import uk.co.sentinelweb.cuer.remote.server.ServerState.INITIAL

class LocalContract {

    interface MviStore : Store<MviStore.Intent, MviStore.State, MviStore.Label> {
        sealed class Intent {
            object Up : Intent()

        }

        sealed class Label {
            object Up : Label()
            data class Message(val msg: String) : Label()

        }

        data class State(
            val serverState: ServerState = INITIAL,
            val serverAddress: String? = null,
            val localNode: NodeDomain? = null,
        )
    }

    interface View : MviView<View.Model, View.Event> {

        fun processLabel(label: MviStore.Label)

        data class Model(
            val title: String,
            val imageUrl: String?,
            val localNode: NodeModel?,
            val address: String?,
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
            object OnUpClicked : Event()
        }
    }
}