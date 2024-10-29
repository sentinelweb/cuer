package uk.co.sentinelweb.cuer.hub.ui.remotes

import androidx.compose.runtime.Composable
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.cast.EmptyCastDialogLauncher
import uk.co.sentinelweb.cuer.app.ui.remotes.*
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore.Label.*
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Model.Companion.Initial
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogContract
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.EmptyChromecastDialogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ext.name
import uk.co.sentinelweb.cuer.hub.ui.home.HomeContract
import uk.co.sentinelweb.cuer.hub.ui.home.HomeContract.HomeModel.DisplayRoute.Folders
import uk.co.sentinelweb.cuer.hub.ui.home.HomeUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.remotes.selector.RemotesDialogLauncher
import uk.co.sentinelweb.cuer.hub.ui.remotes.selector.RemotesDialogLauncherComposeables.ShowRemotesDialogIfNecessary
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator

class RemotesUiCoordinator(
    private val parent: HomeUiCoordinator,
) :
    UiCoordinator<Model>,
    DesktopScopeComponent,
    KoinComponent,
    BaseMviView<Model, Event>(),
    RemotesContract.View {

    override val scope: Scope = desktopScopeWithSource(this)

    override val modelObservable = MutableStateFlow(Initial)

    val remotesDialogLauncher: RemotesDialogContract.Launcher by scope.inject()

    private val controller: RemotesController by scope.inject()
    private val log: LogWrapper by inject()
    private val lifecycle: LifecycleRegistry by inject()

    init {
        log.tag(this)
    }

    override fun create() {
        lifecycle.onCreate()
        controller.onViewCreated(listOf(this), lifecycle)
        lifecycle.onStart()
        lifecycle.onResume()
        controller.onRefresh()
    }

    override fun destroy() {
        lifecycle.onPause()
        lifecycle.onStop()
        lifecycle.onDestroy()
        scope.close()
    }

    @Composable
    fun RemotesDesktopUi() {
        RemotesComposables.RemotesDesktopUi(this)
        ShowRemotesDialogIfNecessary(this.remotesDialogLauncher as RemotesDialogLauncher)
    }

    override fun processLabel(label: RemotesContract.MviStore.Label) {
        log.d("label: $label")
        when (label) {
            is CuerSelectSendTo -> {
                remotesDialogLauncher.launchRemotesDialog(
                    { remoteNodeDomain, screen ->
                        println("selected node: send: ${label.sendNode.name()} to: ${remoteNodeDomain.name()}")
                        dispatch(Event.OnActionSendToSelected(label.sendNode, remoteNodeDomain))
                        remotesDialogLauncher.hideRemotesDialog()
                    },
                    null,
                    true,
                )
            }

            ActionConfig -> {

            }

            is ActionFolders -> {
                parent.go(Folders(label.node))
            }

            else -> Unit
        }
    }

    override fun render(model: Model) {
        log.d("render: ${model.title}")
        this.modelObservable.value = model
    }

    companion object {
        @JvmStatic
        val uiModule = module {
            factory { (parent: HomeUiCoordinator) -> RemotesUiCoordinator(parent) }
            scope(named<RemotesUiCoordinator>()) {
                scoped {
                    RemotesController(
                        storeFactory = get(),
                        modelMapper = get(),
                        lifecycle = get<RemotesUiCoordinator>().lifecycle as Lifecycle,
                        log = get(),
                        wifiStateProvider = get(),
                        remotesRepository = get(),
                        localRepository = get(),
                        coroutines = get(),
                    )
                }
                scoped {
                    RemotesStoreFactory(
//                        storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        storeFactory = DefaultStoreFactory(),
                        log = get(),
                        remoteServerManager = get(),
                        coroutines = get(),
                        localRepository = get(),
                        remoteStatusInteractor = get(),
                        //connectivityWrapper = get(),
                        remotesRepository = get(),
                        locationPermissionLaunch = get(),
                        wifiStateProvider = get(),
                        getPlaylistsFromDeviceUseCase = get(),
                        castController = get(),
                    )
                }
                scoped { RemotesModelMapper(get(), get()) }
                scoped {
                    CastController(
                        cuerCastPlayerWatcher = get(),
                        chromeCastHolder = get(),
                        chromeCastDialogWrapper = EmptyChromecastDialogWrapper(),
                        chromeCastWrapper = get(),
                        floatingManager = get(),
                        playerControls = get(),
                        castDialogLauncher = EmptyCastDialogLauncher(),
                        ytServiceManager = get(),
                        coroutines = get(),
                        log = get()
                    )
                }
            }
        }
    }
}
