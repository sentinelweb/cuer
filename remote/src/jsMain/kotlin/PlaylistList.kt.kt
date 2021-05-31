import kotlinx.html.js.onClickFunction
import react.*
import react.dom.h3
import react.dom.p
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

@JsExport
class PlaylistList : RComponent<PlaylistListProps, RState>() {
    override fun RBuilder.render() {
        h3 {
            +"Playlists"
        }
        for (playlist in props.playlists) {
            p {
                key = playlist.id.toString()
                attrs {
                    onClickFunction = {
                        props.onSelectPlaylist(playlist)
                    }
                }
                if (playlist.id == props.selectedPlaylist?.id) {
                    +"▶ "
                }
                +playlist.title
            }
        }
    }
}

external interface PlaylistListProps : RProps {
    var playlists: List<PlaylistDomain>
    var selectedPlaylist: PlaylistDomain?
    var onSelectPlaylist: (PlaylistDomain) -> Unit
}

fun RBuilder.playlistList(handler: PlaylistListProps.() -> Unit): ReactElement {
    return child(PlaylistList::class) {
        this.attrs(handler)
    }
}
