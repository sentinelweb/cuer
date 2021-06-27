package uk.co.sentinelweb.cuer.app.ui.player

import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlayerController constructor(
    itemLoader: PlayerContract.PlaylistItemLoader,
    storeFactory: StoreFactory = DefaultStoreFactory,
    private val queueConsumer: QueueMediatorContract.Consumer,
    private val modelMapper: PlayerModelMapper,
    private val log: LogWrapper,
    private val coroutineContextProvider: CoroutineContextProvider
) {
    init {
        log.tag(this)
    }

    private val eventToIntent: suspend PlayerContract.View.Event.() -> PlayerContract.MviStore.Intent = {
        when (this) {
            is Initialised -> Load
            is PlayClicked -> Play
            is PauseClicked -> Pause
            is PlayerStateChanged -> PlayState(state)
            is TrackFwdClicked -> TrackFwd
            is TrackBackClicked -> TrackBack
            is SkipFwdClicked -> SkipFwd
            is SkipBackClicked -> SkipBack
        }
    }

    private val trackChangeToIntent: suspend PlaylistItemDomain.() -> PlayerContract.MviStore.Intent = {
        TrackChange(this)
    }

    private val playlistChangeToIntent: suspend PlaylistDomain.() -> PlayerContract.MviStore.Intent = {
        PlaylistChange(this)
    }

    private val store = PlayerStoreFactory(storeFactory, itemLoader, queueConsumer, log).create()

    private var binder: Binder? = null

    fun onViewCreated(view: PlayerContract.View) {
        binder = bind(coroutineContextProvider.Main) {
            store.states.mapNotNull { modelMapper.map(it) } bindTo view
            store.labels bindTo { label -> view.processLabel(label) }

            queueConsumer.currentItemFlow.filterNotNull().mapNotNull { trackChangeToIntent(it) } bindTo store
            queueConsumer.currentPlaylistFlow.filterNotNull().mapNotNull { playlistChangeToIntent(it) } bindTo store
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

