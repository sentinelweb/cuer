package uk.co.sentinelweb.cuer.app.ui.browse

import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapNotNull
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.MviStore.Intent.*
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Event.*
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class BrowseController constructor(
    storeFactory: BrowseStoreFactory,
    private val modelMapper: BrowseModelMapper,
    lifecycle: Lifecycle?,
    log: LogWrapper,
) {
    private val store = storeFactory.create()
    private var binder: Binder? = null

    init {
        log.tag(this)
        lifecycle?.doOnDestroy(store::dispose)
    }

    private val eventToIntent: suspend Event.() -> Intent = {
        when (this) {
            is OnCategoryClicked -> ClickCategory(id = model.id, forceItem = model.forceItem)
            is OnUpClicked -> Up
            is OnResume -> Display
            is OnActionSettingsClicked -> ActionSettings
            is OnSetOrder -> SetOrder(order)
        }
    }

    @ExperimentalCoroutinesApi
    fun onViewCreated(views: List<BrowseContract.View>, viewLifecycle: Lifecycle) {
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