import kotlinx.css.Float
import kotlinx.css.border
import kotlinx.css.float
import kotlinx.html.InputType
import material.Checkbox
import react.*
import styled.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class Banner : RComponent<BannerProps, RState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                put("grid-area", "banner")
            }
            styledH1 {
                css {
                    float = Float.left
                }
                +props.title
            }
            styledDiv {
                css {
                    float = Float.right
                }
                styledInput(type = InputType.text) {
                    css {
                        border = "1px solid black"
                    }
                }
                Checkbox {
                    attrs {
                        id = "rememberMe"
                        checked = true
                        onChange = {/*setRememberMeFlag (! rememberMeFlag)*/ }
                    }
                }
                styledLabel {
                    css {
//                        + AuthorizationFormStyled.checkBoxLabel
                    }
                    attrs["htmlFor"] = "rememberMe"
                    +"Checkbox"
                }
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