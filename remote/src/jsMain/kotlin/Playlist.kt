import App.Companion.NO_IMAGE_SRC
import kotlinx.css.paddingRight
import kotlinx.css.px
import kotlinx.css.width
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.html.ReactHTML.span
import styled.css
import styled.styledDiv
import styled.styledImg
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

@JsExport
class Playlist : RComponent<PlaylistProps, State>() {
    override fun RBuilder.render() {
        styledDiv {
            props.playlist?.items?.forEach { item ->
                styledImg(
                    src = item.media.thumbNail?.url ?: NO_IMAGE_SRC, alt = item.media.title
                ) {
                    css { width = 150.px;paddingRight = 10.px }
                }
                span {
                    item.media.title ?: "No title"
                }
                span {
                    item.media.channelData.title ?: "No Channel"
                }
            }
        }
    }
}

external interface PlaylistProps : Props {
    var title: String
    var playlist: PlaylistDomain?
    var selectedItem: PlaylistItemDomain?
    var onSelectItem: (PlaylistItemDomain) -> Unit
}

fun RBuilder.playlist(handler: PlaylistProps.() -> Unit) {
     child(Playlist::class) {
        this.attrs(handler)
    }
}
