package uk.co.sentinelweb.cuer.app.ui.remotes

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.toGuidIdentifier
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.NodeDomain.DeviceType.OTHER
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.remote.server.ServerState
import uk.co.sentinelweb.cuer.remote.server.ServerState.INITIAL

class RemotesContract {

    interface MviStore : Store<MviStore.Intent, MviStore.State, MviStore.Label> {
        sealed class Intent {
            object Refresh : Intent()
            object Up : Intent()
            object ActionSettings : Intent()
            object ActionPasteAdd : Intent()
            object ActionSearch : Intent()
            object ActionHelp : Intent()
            object ActionStartServer : Intent()
            object ActionStopServer : Intent()
            object ActionPingMulticast : Intent()
            object ActionConfig : Intent()
            object ActionObscuredPerm : Intent()
            data class ActionPingNode(val remote: RemoteNodeDomain) : Intent()
            data class WifiStateChange(val wifiState: WifiStateProvider.WifiState) : Intent()
            data class RemoteUpdate(val remotes: List<RemoteNodeDomain>) : Intent()
            data class LocalUpdate(val local: LocalNodeDomain) : Intent()
            data class RemoteSync(val remote: RemoteNodeDomain) : Intent()
            data class RemoteDelete(val remote: RemoteNodeDomain) : Intent()
            data class RemotePlaylists(val remote: RemoteNodeDomain) : Intent()
        }

        sealed class Label {
            object Up : Label()
            object ActionSettings : Label()
            object ActionSearch : Label()
            object ActionHelp : Label()
            object ActionPasteAdd : Label()
            object ActionConfig : Label()
            data class Message(val msg: String) : Label()

        }

        data class State(
            val serverState: ServerState = INITIAL,
            val serverAddress: String? = null,
            val localNode: LocalNodeDomain,
            val remoteNodes: List<RemoteNodeDomain> = listOf(),
            val wifiState: WifiStateProvider.WifiState,
        )
    }

    interface View : MviView<View.Model, View.Event> {

        fun processLabel(label: MviStore.Label)

        data class Model(
            val title: String,
            val imageUrl: String?,
            val address: String?,
            val localNode: LocalNodeModel,
            val remoteNodes: List<RemoteNodeModel>,
            val serverState: ServerState = INITIAL,
            val wifiState: WifiStateProvider.WifiState,
        ) {
            companion object {
                fun blankModel() = Model(
                    title = "Dummy",
                    imageUrl = "https://cuer-275020.firebaseapp.com/images/headers/remotes.png",
                    localNode = LocalNodeModel.blankModel(),
                    remoteNodes = listOf(),
                    address = "",
                    wifiState = WifiStateProvider.WifiState()
                )
            }
        }

        data class LocalNodeModel(
            val id: OrchestratorContract.Identifier<GUID>?,
            val title: String,
            val address: String,
            val hostname: String,
            val device: String,
            val deviceType: NodeDomain.DeviceType,
            val authType: String,
            val domain: LocalNodeDomain,
        ) {
            companion object {
                fun blankModel() = LocalNodeModel(
                    id = "".toGuidIdentifier(MEMORY),
                    title = "",
                    hostname = "-",
                    address = "",
                    device = "-",
                    deviceType = OTHER,
                    authType = "",
                    domain = LocalNodeDomain(id = null, ipAddress = "", port = -1)
                )
            }
        }

        data class RemoteNodeModel(
            val id: OrchestratorContract.Identifier<GUID>?,
            val title: String,
            val address: String,
            val hostname: String,
            val device: String,
            val deviceType: NodeDomain.DeviceType,
            val authType: String,
            val domain: RemoteNodeDomain,
        )

        sealed class Event {
            object OnRefresh : Event()
            object OnActionSettingsClicked : Event()
            object OnActionPasteAdd : Event()
            object OnActionSearchClicked : Event()
            object OnActionHelpClicked : Event()
            object OnUpClicked : Event()
            object OnActionStartServerClicked : Event()
            object OnActionStopServerClicked : Event()
            object OnActionPingMulticastClicked : Event()
            object OnActionConfigClicked : Event()
            object OnActionObscuredPermClicked : Event()
            data class OnActionPingNodeClicked(val remote: RemoteNodeDomain) : Event()
            data class OnActionDelete(val remote: RemoteNodeDomain) : Event()
            data class OnActionSync(val remote: RemoteNodeDomain) : Event()
            data class OnActionPlaylists(val remote: RemoteNodeDomain) : Event()
        }
    }
}