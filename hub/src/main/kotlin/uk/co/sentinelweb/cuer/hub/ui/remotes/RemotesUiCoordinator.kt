package uk.co.sentinelweb.cuer.hub.ui.remotes

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesController
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesModelMapper
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesStoreFactory
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator

class RemotesUiCoordinator :
    UiCoordinator<RemotesContract.View.Model>,
    DesktopScopeComponent,
    KoinComponent,
    BaseMviView<RemotesContract.View.Model, RemotesContract.View.Event>(),
    RemotesContract.View {

    override val scope: Scope = desktopScopeWithSource(this)

    private val controller: RemotesController by scope.inject()
    private val log: LogWrapper by inject()

    private val lifecycle: LifecycleRegistry by inject()

    private var model: RemotesContract.View.Model? = null
    private var modelObserver: ((RemotesContract.View.Model) -> Unit)? = null

    init {
        log.tag(this)
    }

    override fun create() {
        lifecycle.onCreate()
        controller.onViewCreated(listOf(this), lifecycle)
        lifecycle.onStart()
        lifecycle.onResume()
    }

    override fun destroy() {
        lifecycle.onPause()
        lifecycle.onStop()
        lifecycle.onDestroy()
        scope.close()
    }

    override fun observeModel(updater: (RemotesContract.View.Model) -> Unit) {
        log.d("observeModel: ${model?.title}")
        this.modelObserver = updater
        // todo hack - find a better state device for storing model before observer is set
        // possibly just push an update event to the controller.
        this.model?.also { this.modelObserver?.invoke(it) }
    }

    override fun processLabel(label: RemotesContract.MviStore.Label) {

    }

    override fun render(model: RemotesContract.View.Model) {
        log.d("render: ${model.title}")
        this.model = model
        this.modelObserver?.invoke(model)
    }

    fun loading(isLoading: Boolean) {

    }

    companion object {
        @JvmStatic
        val uiModule = module {
            scope(named<RemotesUiCoordinator>()) {
                scoped {
                    RemotesController(
                        storeFactory = get(),
                        modelMapper = get(),
                        lifecycle = get<RemotesUiCoordinator>().lifecycle as Lifecycle,
                        log = get(),
                        wifiStateProvider = get(),
                        remotesRepository = get(),
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
                        remoteInteractor = get(),
                        connectivityWrapper = get(),
                        remotesRepository = get(),
                        locationPermissionLaunch = get(),
                        wifiStateProvider = get()
                    )
                }
                scoped { RemotesModelMapper(get(), get()) }
            }
        }
    }
}