package uk.co.sentinelweb.cuer.app.ui.remotes

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.NodeDomain

class RemotesContract {

    interface MviStore : Store<MviStore.Intent, MviStore.State, MviStore.Label> {
        sealed class Intent {
            object SendPing : Intent()
            object Up : Intent()
            object ActionSettings : Intent()
            object ActionPasteAdd : Intent()
            object ActionSearch : Intent()
            object ActionHelp : Intent()
        }

        sealed class Label {
            // object None : Label()
            object Up : Label()
            object ActionSettings : Label()
            object ActionSearch : Label()
            object ActionHelp : Label()
            object ActionPasteAdd : Label()

        }

        data class State(
            val nodes: List<NodeDomain> = listOf(),
        )
    }

    interface View : MviView<View.Model, View.Event> {

        fun processLabel(label: MviStore.Label)

        data class Model(
            val title: String,
            val imageUrl: String?,
            val nodes: List<NodeModel>
        )

        data class NodeModel(
            val id: OrchestratorContract.Identifier<GUID>,
            val title: String,

            )

        sealed class Event {
            object OnSendPing : Event()
            object OnActionSettingsClicked : Event()
            object OnActionPasteAdd : Event()
            object OnActionSearchClicked : Event()
            object OnActionHelpClicked : Event()
            object OnUpClicked : Event()
        }
    }
}