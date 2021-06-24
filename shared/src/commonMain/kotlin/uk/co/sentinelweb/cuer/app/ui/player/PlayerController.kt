package uk.co.sentinelweb.cuer.app.ui.player

import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.flow.mapNotNull
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.NONE
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.UNKNOWN

class PlayerController constructor(
    itemLoader: PlayerContract.PlaylistItemLoader,
    storeFactory: StoreFactory = DefaultStoreFactory,
    private val log: LogWrapper,
    private val coroutineContextProvider: CoroutineContextProvider
) {
    init {
        log.tag(this)
    }

    private val stateToModel: suspend PlayerContract.MviStore.State.() -> PlayerContract.View.Model = {
        item?.let {
            PlayerContract.View.Model(
                title = it.media.title,
                platformId = it.media.platformId,
                playState = state,
                playCommand = command
            )
        } ?: PlayerContract.View.Model(null, null, UNKNOWN, NONE)
    }

    private val eventToIntent: suspend PlayerContract.View.Event.() -> PlayerContract.MviStore.Intent = {
        when (this) {
            is Initialised -> Load
            is PlayClicked -> Play
            is PauseClicked -> Pause
            is PlayerStateChanged -> PlayState(state)
        }
    }

    private val store = PlayerStoreFactory(storeFactory, itemLoader, log).create()

    private var binder: Binder? = null

    fun onViewCreated(view: PlayerContract.View) {
        binder = bind(coroutineContextProvider.Main) {
            store.states.mapNotNull(stateToModel) bindTo view
            // Use store.labels to bind Labels to a consumer
            view.events.mapNotNull(eventToIntent) bindTo store
        }
    }

    fun onStart() {
        //log.d("onStart:"+binder)
        binder?.start()
    }

    fun onStop() {
        binder?.stop()
    }

    fun onViewDestroyed() {
        binder = null
    }

    fun onDestroy() {
        store.dispose()
    }
}