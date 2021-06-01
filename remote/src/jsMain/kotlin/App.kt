import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import react.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseResponse

@JsExport
class App : RComponent<RProps, AppState>() {
    override fun RBuilder.render() {
        banner { title = "Cuer" }

        playlistList {
            playlists = state.playlists
            selectedPlaylist = state.currentPlaylist
            onSelectPlaylist = { playlist ->
                if (state.currentPlaylist == null || state.currentPlaylist?.id != playlist.id) {
                    MainScope().launch {
                        val playlistDomain = fetchPlaylist(playlist.id!!)
                        console.log(playlistDomain)
                        setState {
                            currentPlaylist = playlistDomain
                        }
                    }
                }
            }
        }

        playlist {
            title = state.currentPlaylist?.title ?: "No playlist"
            playlist = state.currentPlaylist
            selectedItem = state.currentItem
            onSelectItem = { item ->
                setState {
                    currentItem = item
                }
            }
        }

        state.currentItem?.let { item ->
            videoPlayer {
                video = item.media
                unwatchedVideo = item.media.watched
                onWatchedButtonPressed = {
                    setState {
                        currentItem = currentItem?.let { it.copy(media = it.media.copy(watched = !it.media.watched)) }
                    }
                }
            }
        }

    }

    override fun AppState.init() {
        playlists = listOf()
        currentPlaylist = null

        MainScope().launch {
            val videos = fetchPlaylists()
            setState {
                playlists = videos
            }
        }
    }
}

external interface AppState : RState {
    var currentItem: PlaylistItemDomain?
    var playlists: List<PlaylistDomain>
    var currentPlaylist: PlaylistDomain?
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
