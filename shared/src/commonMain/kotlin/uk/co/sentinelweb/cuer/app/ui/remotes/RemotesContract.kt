package uk.co.sentinelweb.cuer.app.ui.remotes

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import kotlinx.coroutines.flow.StateFlow
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.toGuidIdentifier
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.NodeDomain.DeviceType.OTHER
import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain.Screen
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
            data class ActionSendTo(val sendNode: RemoteNodeDomain) : Intent()
            data class ActionSendToSelected(val sendNode: RemoteNodeDomain, val target: RemoteNodeDomain) : Intent()
            data class WifiStateChange(val wifiState: WifiStateProvider.WifiState) : Intent()
            data class RemoteUpdate(val remotes: List<RemoteNodeDomain>) : Intent()
            data class LocalUpdate(val local: LocalNodeDomain) : Intent()
            data class RemoteSync(val remote: RemoteNodeDomain) : Intent()
            data class RemoteDelete(val remote: RemoteNodeDomain) : Intent()
            data class RemotePlaylists(val remote: RemoteNodeDomain) : Intent()
            data class RemoteFolders(val remote: RemoteNodeDomain) : Intent()
            data class CuerConnect(val remote: RemoteNodeDomain) : Intent()
            data class CuerConnectScreen(val remote: RemoteNodeDomain, val screen: Screen?) : Intent()
        }

        sealed class Label {
            object None : Label()
            object Up : Label()
            object ActionSettings : Label()
            object ActionSearch : Label()
            object ActionHelp : Label()
            object ActionPasteAdd : Label()
            object ActionConfig : Label()
            data class ActionFolders(val node: RemoteNodeDomain) : Label()
            data class Message(val msg: String) : Label()
            data class CuerSelectScreen(val node: RemoteNodeDomain) : Label()
            data class CuerConnected(val remote: RemoteNodeDomain, val screen: Screen?) : Label()
            data class CuerSelectSendTo(val sendNode: RemoteNodeDomain) : Label()
            data class Error(val message:String) : Label()

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
        val modelObservable: StateFlow<Model>

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
                val Initial = Model(
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
            val id: Identifier<GUID>?,
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
            val id: Identifier<GUID>?,
            val title: String,
            val address: String,
            val hostname: String,
            val device: String,
            val deviceType: NodeDomain.DeviceType,
            val authType: String,
            val domain: RemoteNodeDomain,
            val screens: List<Screen>,
        )

        data class Screen(
            val index: Int,
            val width: Int,
            val height: Int,
            val refreshRate: Int,
            val name: String,
            val domain: PlayerNodeDomain.Screen
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
            data class OnActionSendTo(val sendNode: RemoteNodeDomain) : Event()
            data class OnActionSendToSelected(val sendNode: RemoteNodeDomain, val target: RemoteNodeDomain) : Event()
            data class OnActionDelete(val remote: RemoteNodeDomain) : Event()
            data class OnActionSync(val remote: RemoteNodeDomain) : Event()
            data class OnActionPlaylists(val remote: RemoteNodeDomain) : Event()
            data class OnActionFolders(val remote: RemoteNodeDomain) : Event()
            data class OnActionCuerConnect(val remote: RemoteNodeDomain) : Event()
            data class OnActionCuerConnectScreen(val remote: RemoteNodeDomain, val screen: PlayerNodeDomain.Screen?) :
                Event()
        }
    }
}
