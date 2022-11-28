package uk.co.sentinelweb.cuer.app.ui.playlists

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.View.Event
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

@ExperimentalCoroutinesApi
class PlaylistsMviController constructor(
    private val modelMapper: PlaylistsMviModelMapper,
    private val log: LogWrapper,
    private val store: PlaylistsMviContract.MviStore,
    private val playlistOrchestrator: PlaylistOrchestrator,
    lifecycle: Lifecycle?,
) {
    private var binder: Binder? = null

    init {
        log.tag(this)
        lifecycle?.doOnDestroy {
            store.dispose()
        }
    }

    fun onRefresh(scope: CoroutineScope) {
        scope.launch {
            store.accept(Intent.Refresh)
        }
    }

    private val eventToIntent: suspend Event.() -> Intent = {
        when (this) {
            Event.OnClearMove -> Intent.ClearMove
            Event.OnCreatePlaylist -> Intent.CreatePlaylist
            is Event.OnDelete -> Intent.Delete(item)
            is Event.OnEdit -> Intent.Edit(item, view)
            is Event.OnMerge -> Intent.Merge(item)
            is Event.OnMove -> Intent.Move(fromPosition, toPosition)
            is Event.OnMoveSwipe -> Intent.MoveSwipe(item)
            is Event.OnOpenPlaylist -> Intent.OpenPlaylist(item, view)
            is Event.OnPlay -> Intent.Play(item, external, view)
            Event.OnRefresh -> Intent.Refresh
            is Event.OnShare -> Intent.Share(item)
            is Event.OnStar -> Intent.Star(item)
            is Event.OnUndo -> Intent.Undo(undoType)
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
        playlistOrchestrator.updates.mapNotNull { Intent.Refresh } bindTo store
    }

    fun onViewDestroyed() {
        binder = null
    }
}
