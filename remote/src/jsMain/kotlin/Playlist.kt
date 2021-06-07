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
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

@JsExport
class Playlist : RComponent<PlaylistProps, RState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                put("grid-area", "playlist")
                overflowY = Overflow.scroll
            }
            mListSubheader(props.title, disableSticky = true)
            props.playlist?.items?.forEach { item ->
                mListItem(button = true, onClick = { props.onSelectItem(item) }, selected = item == props.selectedItem) {
                    styledImg(
                        src = item.media.thumbNail?.url ?: NO_IMAGE_SRC, alt = item.media.title
                    ) {
                        css { width = 150.px;paddingRight = 10.px }
                    }
                    mListItemText(
                        item.media.title ?: "No title",
                        item.media.channelData?.title ?: "No Channel"
                    )
                }
            }
        }
    }
}

external interface PlaylistProps : RProps {
    var title: String
    var playlist: PlaylistDomain?
    var selectedItem: PlaylistItemDomain?
    var onSelectItem: (PlaylistItemDomain) -> Unit
}

fun RBuilder.playlist(handler: PlaylistProps.() -> Unit): ReactElement {
    return child(Playlist::class) {
        this.attrs(handler)
    }
}
