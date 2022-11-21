package uk.co.sentinelweb.cuer.app.ui.playlists

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapNotNull
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.MviStore.Intent
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

@ExperimentalCoroutinesApi
class PlaylistsMviController constructor(
    private val modelMapper: PlayerModelMapper,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper,
    private val store: PlaylistsMviContract.MviStore,
    lifecycle: Lifecycle?,
) {
    init {
        log.tag(this)
        lifecycle?.doOnDestroy { store.dispose() }
    }

    private val eventToIntent: suspend PlaylistsMviContract.View.Event.() -> Intent = {
        when (this) {
//            is PlayerStateChanged -> PlayState(state)
            else -> Intent.Load
        }
    }
//
//    private val trackChangeToIntent: suspend PlaylistItemDomain.() -> Intent = {
//        TrackChange(this)
//    }
//
//    private val playlistChangeToIntent: suspend PlaylistDomain.() -> Intent = {
//        PlaylistChange(this)
//    }

    private var binder: Binder? = null

    @ExperimentalCoroutinesApi
    fun onViewCreated(views: List<PlaylistsMviContract.View>) {
        if (binder != null) throw IllegalStateException("Already bound")
        binder = bind(coroutines.Main) {
            bindTheThings(views)
        }
    }

    @ExperimentalCoroutinesApi
    fun onViewCreated(views: List<PlaylistsMviContract.View>, viewLifecycle: Lifecycle) {
        if (binder != null) throw IllegalStateException("Already bound")
        binder = bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            bindTheThings(views)
        }
    }

    private fun BindingsBuilder.bindTheThings(views: List<PlaylistsMviContract.View>) {
        views.forEach { view ->
            // store -> view
            store.states.mapNotNull { modelMapper.map(it) } bindTo view
            store.labels bindTo { label -> view.processLabel(label) }

            // view -> store
            view.events.mapNotNull(eventToIntent) bindTo store
        }
//        playControls.intentFlow bindTo store
//        // queue -> store
//        queueConsumer.currentItemFlow
//            .filterNotNull()
//            .mapNotNull { trackChangeToIntent(it) } bindTo store
//        queueConsumer.currentPlaylistFlow
//            .filterNotNull()
//            .mapNotNull { playlistChangeToIntent(it) } bindTo store
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

