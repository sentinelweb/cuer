import App.Companion.NO_IMAGE_SRC
import kotlinx.css.paddingRight
import kotlinx.css.px
import kotlinx.css.width
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.div
import styled.css
import styled.styledDiv
import styled.styledImg
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

@JsExport
class PlaylistList : RComponent<PlaylistListProps, State>() {
    override fun RBuilder.render() {
        styledDiv {
            div { "Playlists" }
            for (playlist in props.playlists) {
                div {
                    ((playlist.thumb?.url ?: playlist.image?.url)
                        // see issue https://github.com/sentinelweb/cuer/issues/186 - need to cache pixabay images somewhere
                        ?.takeIf { !it.startsWith("gs") && !it.startsWith("https://pixabay.com") }
                        ?: NO_IMAGE_SRC)
                        //?.let { mListItemAvatar(src = it, variant = MAvatarVariant.square) }
                        .let {
                            styledImg(src = it, alt = playlist.title) {
                                css {
                                    width = 100.px;paddingRight = 10.px
                                }
                            }
                        }

                    div { playlist.title }
                }
            }
        }
    }
}

external interface PlaylistListProps : Props {
    var playlists: List<PlaylistDomain>
    var selectedPlaylist: PlaylistDomain?
    var onSelectPlaylist: (PlaylistDomain) -> Unit
}

fun RBuilder.playlistList(handler: PlaylistListProps.() -> Unit) {
    child(PlaylistList::class) {
        this.attrs(handler)
    }
}
