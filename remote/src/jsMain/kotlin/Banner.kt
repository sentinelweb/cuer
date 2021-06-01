import react.*
import react.dom.h1
import styled.css
import styled.styledDiv
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class Banner : RComponent<BannerProps, RState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                put("grid-area", "banner")
            }
            h1 {
                +props.title
            }
        }
    }
}

external interface BannerProps : RProps {
    var title: String
    var playlist: PlaylistDomain?
    var selectedItem: PlaylistItemDomain?
    var onSelectItem: (PlaylistItemDomain) -> Unit
}

fun RBuilder.banner(handler: BannerProps.() -> Unit): ReactElement {
    return child(Banner::class) {
        this.attrs(handler)
    }
}