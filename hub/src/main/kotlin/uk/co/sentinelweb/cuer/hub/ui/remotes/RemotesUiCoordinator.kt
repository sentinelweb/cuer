package uk.co.sentinelweb.cuer.hub.ui.remotes

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
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesController
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesModelMapper
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesStoreFactory
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.hub.ui.local.LocalUiCoordinator
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator

class RemotesUiCoordinator :
    UiCoordinator<Model>,
    DesktopScopeComponent,
    KoinComponent,
    BaseMviView<Model, Event>(),
    RemotesContract.View {

    override val scope: Scope = desktopScopeWithSource(this)

    override var modelObservable = MutableStateFlow(Model.blankModel())
        private set

    private val controller: RemotesController by scope.inject()
    private val log: LogWrapper by inject()
    private val lifecycle: LifecycleRegistry by inject()

    private var _localCoordinator: LocalUiCoordinator? = null

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

    override fun processLabel(label: RemotesContract.MviStore.Label) {
        log.d("label: $label")
    }

    override fun render(model: Model) {
        log.d("render: ${model.title}")
        this.modelObservable.value = model
    }

    fun localCoordinator(): LocalUiCoordinator {
        if (_localCoordinator == null) {
            _localCoordinator = getKoin().get()
            _localCoordinator?.create()
        }
        return _localCoordinator ?: throw IllegalStateException("_localCoordinator is null")
    }

    fun destroyLocalCoordinator() {
        if (_localCoordinator != null) {
            _localCoordinator?.destroy()
            _localCoordinator = null
        }
    }

    companion object {
        @JvmStatic
        val uiModule = module {
            factory { RemotesUiCoordinator() }
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
                        strings = get(),
                        log = get(),
                        prefs = get(),
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
                scoped { CastController(get(), get(), get(), get()) }
            }
        }
    }
}