package uk.co.sentinelweb.cuer.hub.ui.local

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
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.Model.Companion.blankModel
import uk.co.sentinelweb.cuer.app.ui.local.LocalController
import uk.co.sentinelweb.cuer.app.ui.local.LocalModelMapper
import uk.co.sentinelweb.cuer.app.ui.local.LocalStoreFactory
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator

class LocalUiCoordinator :
    LocalContract.View,
    UiCoordinator<Model>,

    BaseMviView<Model, Event>(),
    DesktopScopeComponent,
    KoinComponent {

    override val scope: Scope = desktopScopeWithSource(this)

    override var modelObservable = MutableStateFlow(blankModel())
        private set

    private val controller: LocalController by scope.inject()
    private val log: LogWrapper by inject()
    private val lifecycle: LifecycleRegistry by inject()

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

    override fun processLabel(label: LocalContract.MviStore.Label) {
        log.d("label: $label")
    }

    override fun render(model: Model) {
        log.d("label: $model")
        this.modelObservable.value = model
    }

    companion object {
        @JvmStatic
        val uiModule = module {
            factory { LocalUiCoordinator() }
            scope(named<LocalUiCoordinator>()) {
                scoped {
                    LocalController(
                        storeFactory = get(),
                        modelMapper = get(),
                        coroutines = get(),
                        lifecycle = get<LocalUiCoordinator>().lifecycle as Lifecycle,
                        log = get()
                    )
                }
                scoped {
                    LocalStoreFactory(
//                        storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        storeFactory = DefaultStoreFactory(),
                        log = get(),
                        remoteServerManager = get(),
                        localRepository = get(),
                        wifiStateProvider = get(),
                    )
                }
                scoped { LocalModelMapper(get(), get()) }
            }
        }
    }
}