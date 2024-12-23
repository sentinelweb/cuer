package uk.co.sentinelweb.cuer.app.ui.remotes

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository

class RemotesController(
    storeFactory: RemotesStoreFactory,
    private val modelMapper: RemotesModelMapper,
    private val wifiStateProvider: WifiStateProvider,
    private val remotesRepository: RemotesRepository,
    private val localRepository: LocalRepository,
    lifecycle: Lifecycle?,
    private val log: LogWrapper,
    private val coroutines: CoroutineContextProvider,
) {
    private val store = storeFactory.create()
    private var binder: Binder? = null

    init {
        log.tag(this)
        lifecycle?.doOnDestroy {
            store.dispose()
            coroutines.cancel()
        }
    }

    fun onRefresh() {
        coroutines.mainScope.launch {
            delay(300)
            store.accept(Intent.Refresh)
        }
    }

    private val eventToIntent: suspend Event.() -> Intent = {
        when (this) {
            Event.OnRefresh -> Intent.Refresh
            Event.OnActionHelpClicked -> Intent.ActionHelp
            Event.OnActionPasteAdd -> Intent.ActionPasteAdd
            Event.OnActionSearchClicked -> Intent.ActionSearch
            Event.OnActionSettingsClicked -> Intent.ActionSettings
            Event.OnUpClicked -> Intent.Up
            Event.OnActionConfigClicked -> Intent.ActionConfig
            Event.OnActionPingMulticastClicked -> Intent.ActionPingMulticast
            is Event.OnActionPingNodeClicked -> Intent.ActionPingNode(remote)
            Event.OnActionStartServerClicked -> Intent.ActionStartServer
            Event.OnActionStopServerClicked -> Intent.ActionStopServer
            Event.OnActionObscuredPermClicked -> Intent.ActionObscuredPerm
            is Event.OnActionDelete -> Intent.RemoteDelete(remote)
            is Event.OnActionSync -> Intent.RemoteSync(remote)
            is Event.OnActionPlaylists -> Intent.RemotePlaylists(remote)
            is Event.OnActionFolders -> Intent.RemoteFolders(remote)
            is Event.OnActionCuerConnect -> Intent.CuerConnect(remote)
            is Event.OnActionCuerConnectScreen -> Intent.CuerConnectScreen(remote, screen)
            is Event.OnActionSendTo -> Intent.ActionSendTo(sendNode)
            is Event.OnActionSendToSelected -> Intent.ActionSendToSelected(sendNode, target)
            is Event.OnActionEditAddress -> Intent.EditAddress(remote, newAddress)
        }
    }

    @ExperimentalCoroutinesApi
    fun onViewCreated(views: List<RemotesContract.View>, viewLifecycle: Lifecycle) {
        if (binder != null) throw IllegalStateException("Already bound")
        binder =
            bind(viewLifecycle, BinderLifecycleMode.START_STOP, mainContext = coroutines.mainScope.coroutineContext) {
            views.forEach { view ->
                // store -> view
                store.states.mapNotNull { modelMapper.map(it) } bindTo view
                store.labels bindTo { label -> view.processLabel(label) }

                // view -> store
                view.events
                    .onEach { println("Event: $it") }
                    .mapNotNull(eventToIntent) bindTo store

                wifiStateProvider.wifiStateFlow
                    .onEach { log.d("wifi state: $it") }
                    .map { Intent.WifiStateChange(it) } bindTo store

                remotesRepository.updatesFlow
                    .map { Intent.RemoteUpdate(it) } bindTo store

                localRepository.updatesFlow
                    .map { Intent.LocalUpdate(it) } bindTo store

                log.d("binding")
            }
        }

    }
}
