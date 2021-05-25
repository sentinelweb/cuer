
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.h3
import react.dom.p
import uk.co.sentinelweb.cuer.domain.MediaDomain

@JsExport
class VideoList : RComponent<VideoListProps, RState>() {
    override fun RBuilder.render() {
        h3 {
            +props.title
        }
        for (video in props.videos) {
            p {
                key = video.id.toString()
                attrs {
                    onClickFunction = {
                        props.onSelectVideo(video)
                    }
                }
                if (video == props.selectedVideo) {
                    +"▶ "
                }
                +"${video.channelData.title}: ${video.title}"
            }
        }
    }
}

external interface VideoListProps : RProps {
    var title: String
    var videos: List<MediaDomain>
    var selectedVideo: MediaDomain?
    var onSelectVideo: (MediaDomain) -> Unit
}

fun RBuilder.videoList(handler: VideoListProps.() -> Unit): ReactElement {
    return child(VideoList::class) {
        this.attrs(handler)
    }
}
