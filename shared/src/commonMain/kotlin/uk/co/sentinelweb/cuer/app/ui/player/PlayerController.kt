package uk.co.sentinelweb.cuer.app.ui.player

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.service.remote.player.PlayerSessionListener
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent.PlaylistChange
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent.TrackChange
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

@ExperimentalCoroutinesApi
class PlayerController(
    private val queueConsumer: QueueMediatorContract.Consumer,
    private val modelMapper: PlayerModelMapper,
    private val coroutines: CoroutineContextProvider,
    private val mediaSessionListener: MediaSessionListener,
    private val playSessionListener: PlayerSessionListener,
    private val log: LogWrapper,
    private val store: PlayerContract.MviStore,
    lifecycle: Lifecycle?,
) {
    init {
        log.tag(this)
        lifecycle?.doOnDestroy { store.dispose() }
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
        binder =
            bind(viewLifecycle, BinderLifecycleMode.START_STOP, mainContext = coroutines.mainScope.coroutineContext) {
            bindTheThings(views)
        }
    }

    private fun BindingsBuilder.bindTheThings(views: List<PlayerContract.View>) {
        views.forEach { view ->
            // store -> view
            store.states.mapNotNull { modelMapper.map(it) } bindTo view
            store.labels bindTo { label -> view.processLabel(label) }

            // view -> store
            view.events.mapNotNull(PlayerEventToIntentMapper.eventToIntent) bindTo store
        }
        mediaSessionListener.intentFlow bindTo store
        playSessionListener.intentFlow bindTo store
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

