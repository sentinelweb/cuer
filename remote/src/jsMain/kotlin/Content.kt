//import kotlinext.js.jsObject
import kotlinx.css.Visibility
import react.*
import styled.styledDiv
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

external interface ContentProps : Props {
    var title: String
    var loading: Visibility
    var playlists: List<PlaylistDomain>
    var onSelectPlaylist: (PlaylistDomain) -> Unit
    var currentPlaylist: PlaylistDomain?
}

external interface ContentState : State {
    var checkBoxChecked: Boolean
    var currentItem: PlaylistItemDomain?
}

class Content : RComponent<ContentProps, ContentState>() {

    override fun RBuilder.render() {
        styledDiv {


            playlistList {
                playlists = props.playlists
                onSelectPlaylist = {
                    props.onSelectPlaylist(it)
                }
            }


            styledDiv {
                playlist {
                    title = props.currentPlaylist?.title ?: "No playlist"
                    playlist = props.currentPlaylist
                    selectedItem = state.currentItem
                    onSelectItem = { item ->
                        setState {
                            currentItem = item
                        }
                    }
                }
            }
            styledDiv {
                state.currentItem?.let { item ->
                    playlistItem {
                        video = item.media
                        unwatchedVideo = item.media.watched
                        onWatchedButtonPressed = {
                            setState {
                                currentItem =
                                    currentItem?.let { it.copy(media = it.media.copy(watched = !it.media.watched)) }
                            }
                        }
                    }
                }
            }

        }
    }

    override fun ContentState.init() {
        checkBoxChecked = false
        currentItem = null
    }

}

fun RBuilder.content(handler: ContentProps.() -> Unit) = child(Content::class) { attrs(handler) }

