import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.css.Visibility
import react.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseResponse

@JsExport
class App : RComponent<RProps, AppState>() {
    override fun RBuilder.render() {
        content {
            title = "Cuer"
            loading = state.loadingVisibitity
            playlists = state.playlists
            currentPlaylist = state.currentPlaylist
            onSelectPlaylist = { playlist ->
                if (state.currentPlaylist == null || state.currentPlaylist?.id != playlist.id) {
                    setState {
                        loadingVisibitity = Visibility.visible
                    }
                    MainScope().launch {
                        val playlistDomain = fetchPlaylist(playlist.id!!)
                        console.log(playlistDomain)
                        setState {
                            currentPlaylist = playlistDomain
                            loadingVisibitity = Visibility.hidden
                        }
                    }
                }
            }
        }
    }

    override fun AppState.init() {
        playlists = listOf()
        currentPlaylist = null
        loadingVisibitity = Visibility.visible
        MainScope().launch {
            val videos = fetchPlaylists()
            setState {
                playlists = videos
                loadingVisibitity = Visibility.hidden
            }
        }
    }
}

external interface AppState : RState {
    var playlists: List<PlaylistDomain>
    var currentItem: PlaylistItemDomain?
    var currentPlaylist: PlaylistDomain?
    var loadingVisibitity: Visibility
}

suspend fun fetchPlaylists(): List<PlaylistDomain> = coroutineScope {
    val response = window
        .fetch("/playlists")
        .await()
        .text()
        .await()

    deserialiseResponse(response).let {
        it.payload as List<PlaylistDomain>
    }
}

suspend fun fetchPlaylist(id: Long): PlaylistDomain = coroutineScope {
    val response = window
        .fetch("/playlist/$id")
        .await()
        .text()
        .await()

    deserialiseResponse(response).let {
        (it.payload as List<PlaylistDomain>).get(0)
    }
}
