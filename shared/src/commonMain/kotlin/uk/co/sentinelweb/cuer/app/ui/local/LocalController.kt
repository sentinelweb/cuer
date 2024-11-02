package uk.co.sentinelweb.cuer.app.ui.local

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
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.Event
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.remote.server.LocalRepository

class LocalController constructor(
    storeFactory: LocalStoreFactory,
    private val modelMapper: LocalModelMapper,
    private val coroutines: CoroutineContextProvider,
    private val localRepository: LocalRepository,
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
            Event.OnUpClicked -> Intent.Up
            is Event.OnActionSaveClicked -> Intent.ActionSave(updated)
        }
    }

    @ExperimentalCoroutinesApi
    fun onViewCreated(views: List<LocalContract.View>, viewLifecycle: Lifecycle) {
        if (binder != null) throw IllegalStateException("Already bound")
        binder = bind(viewLifecycle, BinderLifecycleMode.START_STOP, mainContext = coroutines.mainScope.coroutineContext) {
            views.forEach { view ->
                // store -> view
                store.states.mapNotNull { modelMapper.map(it) } bindTo view
                store.labels bindTo { label -> view.processLabel(label) }
                localRepository.updatesFlow.mapNotNull { Intent.Update(it) } bindTo store
                // view -> store
                view.events
                    .onEach { println("Event: $it") }
                    .mapNotNull(eventToIntent) bindTo store
            }
        }
    }
}
