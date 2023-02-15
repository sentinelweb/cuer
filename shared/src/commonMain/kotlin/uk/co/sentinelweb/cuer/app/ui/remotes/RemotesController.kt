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
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class RemotesController constructor(
    storeFactory: RemotesStoreFactory,
    private val modelMapper: RemotesModelMapper,
    lifecycle: Lifecycle?,
    log: LogWrapper,
) {
    private val store = storeFactory.create()
    private var binder: Binder? = null

    init {
        log.tag(this)
        lifecycle?.doOnDestroy { store.dispose() }
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
        }
    }

    @ExperimentalCoroutinesApi
    fun onViewCreated(views: List<RemotesContract.View>, viewLifecycle: Lifecycle) {
        if (binder != null) throw IllegalStateException("Already bound")
        binder = bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            views.forEach { view ->
                // store -> view
                store.states.mapNotNull { modelMapper.map(it) } bindTo view
                store.labels bindTo { label -> view.processLabel(label) }

                // view -> store
                view.events
                    .onEach { println("Event: $it") }
                    .mapNotNull(eventToIntent) bindTo store
            }
        }
    }

}