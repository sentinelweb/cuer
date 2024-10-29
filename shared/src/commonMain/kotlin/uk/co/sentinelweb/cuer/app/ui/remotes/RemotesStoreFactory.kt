package uk.co.sentinelweb.cuer.app.ui.remotes

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore.*
import uk.co.sentinelweb.cuer.app.usecase.GetPlaylistsFromDeviceUseCase
import uk.co.sentinelweb.cuer.app.util.permission.LocationPermissionLaunch
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.remote.RemoteStatusInteractor
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.ServerState
import uk.co.sentinelweb.cuer.remote.server.http
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage.MsgType.Ping

class RemotesStoreFactory(
    private val storeFactory: StoreFactory = DefaultStoreFactory(),
    private val log: LogWrapper,
    private val remoteServerManager: RemoteServerContract.Manager,
    private val coroutines: CoroutineContextProvider,
    private val localRepository: LocalRepository,
    private val remoteStatusInteractor: RemoteStatusInteractor,
    private val remotesRepository: RemotesRepository,
    private val locationPermissionLaunch: LocationPermissionLaunch,
    private val wifiStateProvider: WifiStateProvider,
    private val getPlaylistsFromDeviceUseCase: GetPlaylistsFromDeviceUseCase,
    private val castController: CastController,
) {

    init {
        log.tag(this)
    }

    private sealed class Result {
        data class SetNodes(val nodes: List<RemoteNodeDomain>) : Result()
        data class UpdateWifiState(val state: WifiStateProvider.WifiState) : Result()
        object UpdateServerState : Result()
    }

    private sealed class Action {
        object Init : Action()
    }

    private inner class ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(msg: Result): State =
            when (msg) {
                is Result.SetNodes -> copy(remoteNodes = msg.nodes)
                is Result.UpdateServerState -> copy(
                    serverState = if (remoteServerManager.isRunning()) ServerState.STARTED else ServerState.STOPPED,
                    serverAddress = remoteServerManager.getService()?.localNode?.http(),
                    localNode = localRepository.localNode, // remoteServerManager.getService()?.localNode ?:
                    wifiState = wifiStateProvider.wifiState
                ).also { log.d("it.serverAddress; ${it.serverAddress}") }

                is Result.UpdateWifiState -> copy(wifiState = msg.state)
            }
    }

    private class BootstrapperImpl() : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Action.Init)
        }
    }

    private inner class ExecutorImpl() : CoroutineExecutor<Intent, Action, State, Result, Label>() {

        override fun executeAction(action: Action, getState: () -> State) =
            when (action) {
                Action.Init -> init()
            }

        override fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                Intent.ActionSettings -> publish(Label.ActionSettings)
                Intent.ActionPasteAdd -> publish(Label.ActionPasteAdd)
                Intent.ActionSearch -> publish(Label.ActionSearch)
                Intent.ActionHelp -> publish(Label.ActionHelp)
                Intent.Up -> publish(Label.Up)
                Intent.ActionConfig -> config(intent)
                Intent.ActionPingMulticast -> pingMulticast(intent)
                Intent.ActionStartServer -> startServer(intent)
                Intent.ActionStopServer -> stopServer(intent)
                Intent.Refresh -> refresh()
                is Intent.ActionPingNode -> pingNode(intent)
                is Intent.WifiStateChange -> wifiStateChange(intent)
                is Intent.ActionObscuredPerm -> launchLocationPermission()
                is Intent.RemoteUpdate -> remotesUpdate(intent)
                is Intent.RemoteDelete -> deleteRemote(intent)
                is Intent.RemoteSync -> syncRemote(intent)
                is Intent.RemotePlaylists -> getRemotePlaylists(intent)
                is Intent.RemoteFolders -> getRemoteFolders(intent)
                is Intent.LocalUpdate -> dispatch(Result.UpdateServerState)
                is Intent.CuerConnect -> cuerConnect(intent)
                is Intent.CuerConnectScreen -> cuerConnectScreen(intent)
                is Intent.ActionSendTo -> sendTo(intent)
                is Intent.ActionSendToSelected -> sendToSelected(intent)
                is Intent.EditAddress -> editAddress(intent)
            }

        private fun editAddress(intent: Intent.EditAddress) {
            coroutines.mainScope.launch {
                remotesRepository.updateAddress(intent.remote, intent.newAddress)
            }
        }

        private fun cuerConnect(intent: Intent.CuerConnect) {
            // fixme check screens
            publish(Label.CuerSelectScreen(intent.remote))
        }

        private fun cuerConnectScreen(intent: Intent.CuerConnectScreen) {
            castController.connectCuerCast(intent.remote, intent.screen)
            publish(Label.CuerConnected(intent.remote, intent.screen))
        }

        private fun deleteRemote(intent: Intent.RemoteDelete) {
            coroutines.mainScope.launch { remotesRepository.removeNode(intent.remote) }
        }

        private fun remotesUpdate(intent: Intent.RemoteUpdate) {
            dispatch(Result.SetNodes(intent.remotes))
        }

        private fun wifiStateChange(intent: Intent.WifiStateChange) {
            dispatch(Result.UpdateWifiState(intent.wifiState))
            log.d("got wifiStateChange: $intent")
            coroutines.mainScope.launch {
                delay(300)
                dispatch(Result.UpdateServerState)
            }
        }

        private fun launchLocationPermission() {
            locationPermissionLaunch.launchLocationPermission()
        }

        private fun pingNode(intent: Intent.ActionPingNode) {
            coroutines.ioScope.launch {
                remotesRepository.addUpdateNode(
                    intent.remote.copy(
                        isAvailable = remoteStatusInteractor.available(Ping, intent.remote).isSuccessful
                    )
                )
            }
        }

        private fun sendToSelected(intent: Intent.ActionSendToSelected) {
            coroutines.ioScope.launch {
                remoteStatusInteractor.sendTo(Ping, intent.sendNode, intent.target).isSuccessful
            }
        }

        private fun sendTo(intent: Intent.ActionSendTo) {
            publish(Label.CuerSelectSendTo(intent.sendNode))
        }

        private fun config(intent: Intent) {
            publish(Label.ActionConfig)
        }

        private fun pingMulticast(intent: Intent) {
            if (remoteServerManager.isRunning()) {
                coroutines.ioScope.launch {
                    remoteServerManager.getService()?.multicastPing()
                    withContext(coroutines.Main) { dispatch(Result.UpdateServerState) } // is this necessary?
                }
            }
        }

        private var remotesJob: Job? = null
        private fun startServer(intent: Intent) {
            if (!remoteServerManager.isRunning()) {
                coroutines.mainScope.launch {
                    remoteServerManager.start()
                    // fixme limit?
                    while (remoteServerManager.getService()?.isServerStarted != true) {
                        print(".")
                        delay(50)
                    }
                    log.d("isRunning ${remoteServerManager.isRunning()} svc: ${remoteServerManager.getService()} address: ${remoteServerManager.getService()?.localNode?.http()}")

                    remoteServerManager.getService()?.stopListener = { dispatch(Result.UpdateServerState) }
                    dispatch(Result.UpdateServerState)
                }
            }
        }

        private fun stopServer(intent: Intent) {
            if (remoteServerManager.isRunning()) {
                coroutines.mainScope.launch {
                    remotesJob?.cancel()
                    remoteServerManager.stop()
                    delay(20)
                    dispatch(Result.UpdateServerState)
                }
            }
        }

        private fun init() {
            dispatch(Result.UpdateServerState)
            if (remoteServerManager.isRunning()) {
                remoteServerManager.getService()?.stopListener = { dispatch(Result.UpdateServerState) }
            }
        }

        private fun refresh() {
            dispatch(Result.UpdateServerState)
            wifiStateProvider.updateWifiInfo()
        }

        private fun syncRemote(intent: Intent.RemoteSync) {
            coroutines.ioScope.launch {
                runCatching {
                    getPlaylistsFromDeviceUseCase.getPlaylists(intent.remote)
                        .map { getPlaylistsFromDeviceUseCase.getPlaylist(it) }
                        //.let { playlistsOrchestrator.save(it, LOCAL.deepOptions()) }
                        .also { log.d(it.toString()) }
                }.onFailure { e ->
                    withContext(coroutines.Main) {
                        publish(Label.Error(e.message?.let { "${intent.remote.hostname}: $it" }
                            ?: "Could not get playlists from: ${intent.remote.hostname}"))
                    }
                }
                //log.d("Not implemented: Sync playlists with: ${intent.remote.ipport()}")
            }
        }

        private fun getRemotePlaylists(intent: Intent.RemotePlaylists) {
            coroutines.ioScope.launch {
                runCatching {
                    val playlists = getPlaylistsFromDeviceUseCase.getPlaylists(intent.remote)
                    log.d(playlists.toString())
                }.onFailure { e ->
                    withContext(coroutines.Main) {
                        publish(Label.Error(e.message?.let { "${intent.remote.hostname}: $it" }
                            ?: "Could not get playlists from: ${intent.remote.hostname}"))
                    }
                }
            }
        }

        private fun getRemoteFolders(intent: Intent.RemoteFolders) {
            intent.remote
                .run { publish(Label.ActionFolders(this)) }
                .run { publish(Label.None) } // stops label re-firing on back from folders
        }
    }

    fun create(): MviStore =
        object : MviStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RemotesStore",
            initialState = State(
                localNode = localRepository.localNode,
                wifiState = wifiStateProvider.wifiState
            ),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl()
        ) {}
}
