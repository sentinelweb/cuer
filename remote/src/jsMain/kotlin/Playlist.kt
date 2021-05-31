import kotlinx.html.js.onClickFunction
import react.*
import react.dom.h3
import react.dom.p
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

@JsExport
class Playlist : RComponent<PlaylistProps, RState>() {
    override fun RBuilder.render() {
        h3 {
            +props.title
        }
        props.playlist?.items?.forEach { item ->
            p {
                key = item.id.toString()
                attrs {
                    onClickFunction = {
                        props.onSelectItem(item)
                    }
                }
                if (item == props.selectedItem) {
                    +"▶ "
                }
                +"${item.media.channelData.title}: ${item.media.title}"
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
