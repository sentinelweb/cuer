package uk.co.sentinelweb.cuer.app.ui.support

import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapNotNull

import uk.co.sentinelweb.cuer.app.ui.support.SupportContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract.View.Event

@ExperimentalCoroutinesApi
class SupportController constructor(
    storeFactory: SupportStoreFactory,
    private val modelMapper: SupportModelMapper
) {
    private val store = storeFactory.create()

    private val eventToIntent: suspend Event.() -> Intent = {
        when (this) {
            is Event.OnLinkClicked -> Intent.Open(link = this.link.domain)
            is Event.Load -> Intent.Load(this.media)
        }
    }

    private var binder: Binder? = null

    @ExperimentalCoroutinesApi
    fun onViewCreated(views: List<SupportContract.View>, viewLifecycle: Lifecycle) {
        if (binder != null) throw IllegalStateException("Already bound")
        binder = bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            views.forEach { view ->
                // store -> view
                store.states.mapNotNull { modelMapper.map(it) } bindTo view
                store.labels bindTo { label -> view.processLabel(label) }

                // view -> store
                view.events.mapNotNull(eventToIntent) bindTo store
            }
        }
    }
}