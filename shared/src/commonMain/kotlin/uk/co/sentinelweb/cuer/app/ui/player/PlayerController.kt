package uk.co.sentinelweb.cuer.app.ui.player

import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.extensions.coroutines.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.Support
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

@ExperimentalCoroutinesApi
class PlayerController constructor(
    private val queueConsumer: QueueMediatorContract.Consumer,
    private val modelMapper: PlayerModelMapper,
    private val coroutines: CoroutineContextProvider,
    private val playControls: PlayerListener,
    private val log: LogWrapper,
    private val store: PlayerContract.MviStore,
    lifecycle: Lifecycle?,
) {
    init {
        log.tag(this)
        lifecycle?.doOnDestroy(store::dispose)
    }

    private val eventToIntent: suspend PlayerContract.View.Event.() -> Intent = {
        when (this) {
            is PlayerStateChanged -> PlayState(state)
            is TrackFwdClicked -> TrackFwd
            is TrackBackClicked -> TrackBack
            is SkipFwdClicked -> SkipFwd
            is SkipBackClicked -> SkipBack
            is PositionReceived -> Position(ms)
            is SkipFwdSelectClicked -> SkipFwdSelect
            is SkipBackSelectClicked -> SkipBackSelect
            is PlayPauseClicked -> PlayPause(isPlaying)
            is SeekBarChanged -> SeekTo(fraction)
            is PlaylistClicked -> PlaylistView
            is ItemClicked -> PlaylistItemView
            is LinkClick -> LinkOpen(url)
            is ChannelClick -> ChannelOpen
            is TrackClick -> TrackSelected(item, resetPosition)
            is DurationReceived -> Duration(ms)
            is IdReceived -> Id(videoId)
            is FullScreenClick -> FullScreenPlayerOpen
            is PortraitClick -> PortraitPlayerOpen
            is PipClick -> PipPlayerOpen
            //is OnDestroy -> {log.d("map destroy");Destroy}
            is OnInitFromService -> InitFromService(item)
            is OnPlayItemFromService -> PlayItemFromService(item)
            is OnSeekToPosition -> SeekToPosition(ms)
            is Support -> Intent.Support
        }
    }

    private val trackChangeToIntent: suspend PlaylistItemDomain.() -> Intent = {
        TrackChange(this)
    }

    private val playlistChangeToIntent: suspend PlaylistDomain.() -> Intent = {
        PlaylistChange(this)
    }

    private var binder: Binder? = null

    @ExperimentalCoroutinesApi
    fun onViewCreated(views: List<PlayerContract.View>) {
        if (binder != null) throw IllegalStateException("Already bound")
        binder = bind(coroutines.Main) {
            bindTheThings(views)
        }
    }

    @ExperimentalCoroutinesApi
    fun onViewCreated(views: List<PlayerContract.View>, viewLifecycle: Lifecycle) {
        if (binder != null) throw IllegalStateException("Already bound")
        binder = bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            bindTheThings(views)
        }
    }

    private fun BindingsBuilder.bindTheThings(views: List<PlayerContract.View>) {
        views.forEach { view ->
            // store -> view
            store.states.mapNotNull { modelMapper.map(it) } bindTo view
            store.labels bindTo { label -> view.processLabel(label) }

            // view -> store
            view.events.mapNotNull(eventToIntent) bindTo store
        }
        playControls.intentFlow
            .onEach { log.d("playctls: $it") } bindTo store
        // queue -> store
        queueConsumer.currentItemFlow
            .filterNotNull()
            .mapNotNull { trackChangeToIntent(it) } bindTo store
        queueConsumer.currentPlaylistFlow
            .filterNotNull()
            .mapNotNull { playlistChangeToIntent(it) } bindTo store
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

    fun onDestroy(endSession:Boolean) {
        if (endSession) {
            store.endSession()
        }
        store.dispose()
    }
}

