package uk.co.sentinelweb.cuer.hub.ui.player.vlc

import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.core.view.BaseMviView
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator

class VlcPlayerUiCoordinator :
    PlayerContract.View,
    UiCoordinator<Model>,
    BaseMviView<Model, Event>(),
    DesktopScopeComponent,
    KoinComponent {

    override val scope: Scope = desktopScopeWithSource(this)

    private val controller: PlayerController by scope.inject()
    private val log: LogWrapper by inject()
    private val lifecycle: LifecycleRegistry by inject()

    override var modelObservable = MutableStateFlow(Model.blankModel())
        private set

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

    override suspend fun processLabel(label: PlayerContract.MviStore.Label) {

    }


    override fun render(model: Model) {
        log.d("label: $model")
        this.modelObservable.value = model
    }

}
