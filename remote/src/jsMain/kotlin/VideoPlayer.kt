import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.h3
import styled.css
import styled.styledButton
import styled.styledDiv

external interface VideoPlayerProps : RProps {
    var video: Video
    var onWatchedButtonPressed: (Video) -> Unit
    var unwatchedVideo: Boolean
}

@JsExport
class VideoPlayer : RComponent<VideoPlayerProps, RState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                position = Position.absolute
                top = 10.px
                right = 10.px
            }
            h3 {
                +"${props.video.speaker}: ${props.video.title}"
            }
            reactPlayerLite {
                attrs.url = props.video.videoUrl
            }
            styledDiv {
                css {
                    display = Display.flex
                    marginBottom = 10.px
                }
                emailShareButton {
                    attrs.url = props.video.videoUrl
                    emailIcon {
                        attrs.size = 32
                        attrs.round = true
                    }
                }
                telegramShareButton {
                    attrs.url = props.video.videoUrl
                    telegramIcon {
                        attrs.size = 32
                        attrs.round = true
                    }
                }
                twitterShareButton {
                    attrs.url = props.video.videoUrl
                    twitterIcon {
                        attrs.size = 32
                        attrs.round = true
                    }
                }
                whatsappShareButton {
                    attrs.url = props.video.videoUrl
                    whatsappIcon {
                        attrs.size = 32
                        attrs.round = true
                    }
                }
                facebookShareButton {
                    attrs.url = props.video.videoUrl
                    facebookIcon {
                        attrs.size = 32
                        attrs.round = true
                    }
                }
                linkedinShareButton {
                    attrs.url = props.video.videoUrl
                    linkedinIcon {
                        attrs.size = 32
                        attrs.round = true
                    }
                }
                pinterestShareButton {
                    attrs.url = props.video.videoUrl
                    pinterestIcon {
                        attrs.size = 32
                        attrs.round = true
                    }
                }
                redditShareButton {
                    attrs.url = props.video.videoUrl
                    redditIcon {
                        attrs.size = 32
                        attrs.round = true
                    }
                }
            }
            styledButton {
                css {
                    display = Display.block
                    backgroundColor = if (props.unwatchedVideo) Color.lightGreen else Color.red
                }
                attrs {
                    onClickFunction = {
                        props.onWatchedButtonPressed(props.video)
                    }
                }
                if (props.unwatchedVideo) {
                    +"Mark as watched"
                } else {
                    +"Mark as unwatched"
                }
            }

        }
    }
}

fun RBuilder.videoPlayer(handler: VideoPlayerProps.() -> Unit): ReactElement {
    return child(VideoPlayer::class) {
        this.attrs(handler)
    }
}