package uk.co.sentinelweb.cuer.app.ui.browse

import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapNotNull
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class BrowserContoller constructor(
    storeFactory: StoreFactory = DefaultStoreFactory,
    private val modelMapper: BrowseModelMapper,
    private val coroutines: CoroutineContextProvider,
    lifecycle: Lifecycle?,
    log: LogWrapper,
) {
    private val store = BrowseStoreFactory(
        storeFactory
    ).create()
    private var binder: Binder? = null

    init {
        log.tag(this)
        lifecycle?.doOnDestroy(store::dispose)
    }

    private val eventToIntent: suspend BrowseContract.View.Event.() -> BrowseContract.MviStore.Intent = {
        when (this) {
            BrowseContract.View.Event.Load -> BrowseContract.MviStore.Intent.Load
            is BrowseContract.View.Event.ClickCategory -> BrowseContract.MviStore.Intent.ClickCategory(id = id)
        }
    }

    @ExperimentalCoroutinesApi
    fun onViewCreated(views: List<BrowseContract.View>, viewLifecycle: Lifecycle) {
        if (binder != null) throw IllegalStateException("Already bound")
        binder = bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            views.forEach { view ->
                // store -> view
                store.states.mapNotNull { modelMapper.map(it) } bindTo view

                // view -> store
                view.events.mapNotNull(eventToIntent) bindTo store
            }
        }
    }

}