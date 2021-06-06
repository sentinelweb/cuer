
import kotlinx.css.*
import kotlinx.html.DIV
import react.*
import react.dom.h3
import react.dom.p
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv
import uk.co.sentinelweb.cuer.domain.MediaDomain

external interface PlaylistItemProps : RProps {
    var video: MediaDomain
    var onWatchedButtonPressed: (MediaDomain) -> Unit
    var unwatchedVideo: Boolean
}

@JsExport
class PlaylistItem : RComponent<PlaylistItemProps, RState>() {

    override fun RBuilder.render() {
        styledDiv {
            css {
                put("grid-area", "item")
                overflowY = Overflow.scroll
            }
            h3 {
                +"${props.video.channelData.title}: ${props.video.title}"
            }
            styledDiv {
                css {
                    display = Display.block
                    width = 80.pct
                }
                reactPlayerLite {
                    attrs.url = props.video.url
                }
            }
            shareButtons()
            styledDiv {
                props.video.description?.split("\n")
                    ?.filter { it != "" }
                    ?.forEach {
                        p {
                            +it
                        }
                    }
            }
        }
    }

    private fun StyledDOMBuilder<DIV>.shareButtons() {
        styledDiv {
            css {
                display = Display.flex
                marginBottom = 10.px
            }
            emailShareButton {
                attrs.url = props.video.url
                emailIcon {
                    attrs.size = 32
                    attrs.round = true
                }
            }
            telegramShareButton {
                attrs.url = props.video.url
                telegramIcon {
                    attrs.size = 32
                    attrs.round = true
                }
            }
            twitterShareButton {
                attrs.url = props.video.url
                twitterIcon {
                    attrs.size = 32
                    attrs.round = true
                }
            }
            whatsappShareButton {
                attrs.url = props.video.url
                whatsappIcon {
                    attrs.size = 32
                    attrs.round = true
                }
            }
            facebookShareButton {
                attrs.url = props.video.url
                facebookIcon {
                    attrs.size = 32
                    attrs.round = true
                }
            }
            linkedinShareButton {
                attrs.url = props.video.url
                linkedinIcon {
                    attrs.size = 32
                    attrs.round = true
                }
            }
            pinterestShareButton {
                attrs.url = props.video.url
                pinterestIcon {
                    attrs.size = 32
                    attrs.round = true
                }
            }
            redditShareButton {
                attrs.url = props.video.url
                redditIcon {
                    attrs.size = 32
                    attrs.round = true
                }
            }
        }
    }
}

fun RBuilder.playlistItem(handler: PlaylistItemProps.() -> Unit): ReactElement {
    return child(PlaylistItem::class) {
        this.attrs(handler)
    }
}
