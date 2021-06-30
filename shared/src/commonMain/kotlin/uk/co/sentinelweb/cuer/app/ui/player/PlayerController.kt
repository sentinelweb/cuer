package uk.co.sentinelweb.cuer.app.ui.player

import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
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
    private val queueProducer: QueueMediatorContract.Producer,
    private val modelMapper: PlayerModelMapper,
    private val coroutines: CoroutineContextProvider,
    private val lifecycle: Lifecycle?,
    skip: SkipContract.External,
    log: LogWrapper
) {
    private val store = PlayerStoreFactory(storeFactory, itemLoader, queueConsumer, queueProducer, skip, coroutines, log).create()

    init {
        log.tag(this)
        lifecycle?.doOnDestroy(store::dispose)
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
            is SendPosition -> Position(ms)
            is SkipFwdSelectClicked -> SkipFwdSelect
            is SkipBackSelectClicked -> SkipBackSelect
            is PlayPauseClicked -> PlayPause(isPlaying)
            is SeekBarChanged -> SeekTo(fraction)
            is PlaylistClicked -> PlaylistView
            is ItemClicked -> PlaylistItemView
            is LinkClick -> LinkOpen(url)
            is ChannelClick -> ChannelOpen
            is TrackClick -> TrackSelected(item, resetPosition)
        }
    }

    private val trackChangeToIntent: suspend PlaylistItemDomain.() -> PlayerContract.MviStore.Intent = {
        TrackChange(this)
    }

    private val playlistChangeToIntent: suspend PlaylistDomain.() -> PlayerContract.MviStore.Intent = {
        PlaylistChange(this)
    }


    private var binder: Binder? = null

    @ExperimentalCoroutinesApi
    fun onViewCreated(views: List<PlayerContract.View>) {
        if (binder != null) throw IllegalStateException("Already bound")
        binder = bind(coroutines.Main) {
            views.forEach { view ->
                // store -> view
                store.states.mapNotNull { modelMapper.map(it) } bindTo view
                store.labels bindTo { label -> view.processLabel(label) }

                // view -> store
                view.events.mapNotNull(eventToIntent) bindTo store
            }
            // queue -> store
            queueConsumer.currentItemFlow.filterNotNull().mapNotNull { trackChangeToIntent(it) } bindTo store
            queueConsumer.currentPlaylistFlow.filterNotNull().mapNotNull { playlistChangeToIntent(it) } bindTo store
        }
    }

    @ExperimentalCoroutinesApi
    fun onViewCreated(views: List<PlayerContract.View>, viewLifecycle: Lifecycle) {
        if (binder != null) throw IllegalStateException("Already bound")
        binder = bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            views.forEach { view ->
                // store -> view
                store.states.mapNotNull { modelMapper.map(it) } bindTo view
                store.labels bindTo { label -> view.processLabel(label) }

                // view -> store
                view.events.mapNotNull(eventToIntent) bindTo store
            }
            // queue -> store
            queueConsumer.currentItemFlow.filterNotNull().mapNotNull { trackChangeToIntent(it) } bindTo store
            queueConsumer.currentPlaylistFlow.filterNotNull().mapNotNull { playlistChangeToIntent(it) } bindTo store
        }
    }

    fun onStart() {
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

