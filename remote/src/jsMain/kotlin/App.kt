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
class App : RComponent<Props, AppState>() {

    override fun AppState.init() {
        playlists = listOf()
        currentPlaylist = null
        loadingVisibitity = Visibility.visible
        MainScope().launch {
            val playlistsFetched = fetchPlaylists()
            setState {
                playlists = playlistsFetched
                loadingVisibitity = Visibility.hidden
            }
            // todo test code - remove
            fetchPlaylist(playlistsFetched.find { it.title == "music" } ?: playlistsFetched[0])
        }
    }

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
                    fetchPlaylist(playlist)
                }
            }
        }
    }

    private fun fetchPlaylist(playlist: PlaylistDomain) {
        MainScope().launch {
            //val playlistDomain = fetchPlaylist(playlist)
            setState {
                currentPlaylist = playlist
                loadingVisibitity = Visibility.hidden
            }
        }
    }

    companion object {
        const val NO_IMAGE_SRC = "/noimage.png"
    }
}

external interface AppState : State {
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
