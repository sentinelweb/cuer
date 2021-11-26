import App.Companion.NO_IMAGE_SRC
import com.ccfraser.muirwik.components.list.mListItem
import com.ccfraser.muirwik.components.list.mListItemText
import com.ccfraser.muirwik.components.list.mListSubheader
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv
import styled.styledImg
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

@JsExport
class PlaylistList : RComponent<PlaylistListProps, State>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                put("grid-area", "playlists")
                overflowY = Overflow.scroll
                overflowX = Overflow.scroll
            }
            mListSubheader("Playlists", disableSticky = true)
            for (playlist in props.playlists) {
                mListItem(
                    button = true,
                    onClick = { props.onSelectPlaylist(playlist) },
                    selected = playlist.id == props.selectedPlaylist?.id
                ) {
                    ((playlist.thumb?.url ?: playlist.image?.url)
                        // see issue https://github.com/sentinelweb/cuer/issues/186 - need to cache pixabay images somewhere
                        ?.takeIf { !it.startsWith("gs") && !it.startsWith("https://pixabay.com") }
                        ?: NO_IMAGE_SRC)
                        //?.let { mListItemAvatar(src = it, variant = MAvatarVariant.square) }
                        .let { styledImg(src = it, alt = playlist.title) { css { width = 100.px;paddingRight = 10.px } } }

                    mListItemText(playlist.title)
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
