
import kotlinx.css.*
import kotlinx.html.DIV
import react.*
import react.dom.h3
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.remote.util.WebLink

external interface PlaylistItemProps : RProps {
    var video: MediaDomain
    var onWatchedButtonPressed: (MediaDomain) -> Unit
    var unwatchedVideo: Boolean
}

@JsExport
class PlaylistItem : RComponent<PlaylistItemProps, RState>() {
    private val webLink = WebLink()

    override fun RBuilder.render() {
        styledDiv {
            css {
                put("grid-area", "item")
                overflowY = Overflow.auto
                fontFamily = "Roboto"
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
//            styledDiv {
//                attrs {
//                    id = ITEM_DESCRIPTION_ID
//                }
//            }
//            document.getElementById(ITEM_DESCRIPTION_ID)?.innerHTML =
//                (props.video.description
//                    ?.let { webLink.replaceLinks(it) }
//                    ?.replace("\n", "<br/>")
//                    ?: "")
        }
    }

    private fun StyledDOMBuilder<DIV>.shareButtons() {
        styledDiv {
            css {
                display = Display.flex
                margin = "10px"
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

    companion object {
        private const val ITEM_DESCRIPTION_ID = "itemDescription"
    }
}

fun RBuilder.playlistItem(handler: PlaylistItemProps.() -> Unit): ReactElement {
    return child(PlaylistItem::class) {
        this.attrs(handler)
    }
}


