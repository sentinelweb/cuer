import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import react.*
import react.dom.h1
import styled.css
import styled.styledDiv
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseResponse

const val BASE_URL = "http://localhost:9090"

@JsExport
class App : RComponent<RProps, AppState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                put("grid-area", "banner")
            }
            h1 {
                +"Cuer playlists"
            }
        }
        styledDiv {
            css {
                put("grid-area", "playlists")
            }
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
        }
        styledDiv {
            css {
                put("grid-area", "playlist")
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
        }
        styledDiv {
            css {
                put("grid-area", "item")
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
        .fetch("/playlists")//$BASE_URL
        .await()
        .text()
        .await()

    deserialiseResponse(response).let {
        it.payload as List<PlaylistDomain>
    }
}

suspend fun fetchPlaylist(id: Long): PlaylistDomain = coroutineScope {
    val response = window
        .fetch("/playlist/$id")//$BASE_URL
        .await()
        .text()
        .await()

    deserialiseResponse(response).let {
        (it.payload as List<PlaylistDomain>).get(0)
    }
}
