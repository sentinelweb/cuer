package uk.co.sentinelweb.cuer.app.ui.playlist

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.View.Event
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

@ExperimentalCoroutinesApi
class PlaylistMviController constructor(
    private val modelMapper: PlaylistMviModelMapper,
    private val log: LogWrapper,
    private val store: PlaylistMviContract.MviStore,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val mediaOrchestrator: MediaOrchestrator,
    lifecycle: Lifecycle?,
) {
    private var binder: Binder? = null

    init {
        log.tag(this)
        lifecycle?.doOnDestroy {
            store.dispose()
        }
    }

    fun onRefresh() {
        CoroutineScope(Dispatchers.Main).launch {
            store.accept(Intent.Refresh)
        }
    }

    private val eventToIntent: suspend Event.() -> Intent = {
        when (this) {
            Event.OnRefresh -> Intent.Refresh
            is Event.OnSetPlaylistData -> Intent.SetPlaylistData(plId, plItemId, playNow, source, addPlaylistParent)
            Event.OnCheckToSave -> Intent.CheckToSave
            Event.OnCommit -> Intent.Commit
            is Event.OnDeleteItem -> Intent.DeleteItem(item = item)
            Event.OnEdit -> Intent.Edit
            is Event.OnGotoPlaylist -> Intent.GotoPlaylist(item = item)
            Event.OnHelp -> Intent.Help
            is Event.OnMove -> Intent.Move(fromPosition = fromPosition, toPosition = toPosition)
            is Event.OnMoveSwipe -> Intent.MoveSwipe(item = item)
            Event.OnClearMove -> Intent.ClearMove
            Event.OnPause -> Intent.Pause
            Event.OnPlay -> Intent.Play
            is Event.OnPlayItem -> Intent.PlayItem(item = item)
            Event.OnPlayModeChange -> Intent.PlayModeChange
            is Event.OnPlaylistSelected -> Intent.PlaylistSelected(playlist = playlist)
            is Event.OnRelatedItem -> Intent.RelatedItem(item = item)
            Event.OnResume -> Intent.Resume
            is Event.OnShareItem -> Intent.ShareItem(item = item)
            is Event.OnShowCards -> Intent.ShowCards(isCards = isCards)
            Event.OnShowChannel -> Intent.ShowChannel
            is Event.OnShowItem -> Intent.ShowItem(item = item)
            Event.OnStar -> Intent.Star
            is Event.OnStarItem -> Intent.StarItem(item = item)
            is Event.OnUndo -> Intent.Undo(undoType = undoType)
            Event.OnUpdate -> Intent.Update
            Event.OnLaunch -> Intent.Launch
            Event.OnShare -> Intent.Share
            Event.OnClearMove -> Intent.ClearMove
        }
    }

    @ExperimentalCoroutinesApi
    fun onViewCreated(views: List<PlaylistMviContract.View>, viewLifecycle: Lifecycle) {
        if (binder != null) throw IllegalStateException("Already bound")
        binder = bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            bindTheThings(views)
        }
    }

    private fun BindingsBuilder.bindTheThings(views: List<PlaylistMviContract.View>) {
        views.forEach { view ->
            // store -> view
            store.states.mapNotNull { modelMapper.map(it) } bindTo view
            store.labels bindTo { label -> view.processLabel(label) }

            // view -> store
            view.events.mapNotNull(eventToIntent) bindTo store
        }
        playlistOrchestrator.updates.mapNotNull { Intent.UpdatesPlaylist(it.first, it.second, it.third) } bindTo store
        playlistItemOrchestrator.updates.mapNotNull {
            Intent.UpdatesPlaylistItem(
                it.first,
                it.second,
                it.third
            )
        } bindTo store
        mediaOrchestrator.updates.mapNotNull { Intent.UpdatesMedia(it.first, it.second, it.third) } bindTo store
    }

    fun onViewDestroyed() {
        binder = null
    }
}
