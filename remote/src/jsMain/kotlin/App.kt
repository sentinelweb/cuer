
import kotlinx.browser.window
import kotlinx.coroutines.*
import react.*
import react.dom.h1
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain

@JsExport
class App : RComponent<RProps, AppState>() {
    override fun RBuilder.render() {
        h1 {
            +"KotlinConf Explorer"
        }
        videoList {
            title = "Unwatched videos"
            videos = state.unwatchedVideos
            selectedVideo = state.currentVideo
            onSelectVideo = { video ->
                setState {
                    currentVideo = if (currentVideo == video)
                        null
                    else video
                }
            }
        }

        videoList {
            title = "Watched videos"
            videos = state.watchedVideos
            selectedVideo = state.currentVideo
            onSelectVideo = { video ->
                setState {
                    currentVideo = video
                }
            }
        }
        state.currentVideo?.let { currentVideo ->
            videoPlayer {
                video = currentVideo
                unwatchedVideo = currentVideo in state.unwatchedVideos
                onWatchedButtonPressed = {
                    if (video in state.unwatchedVideos) {
                        setState {
                            unwatchedVideos -= video
                            watchedVideos += video
                        }
                    } else {
                        setState {
                            watchedVideos -= video
                            unwatchedVideos += video
                        }
                    }
                }
            }
        }
    }

    override fun AppState.init() {
        unwatchedVideos = listOf()
        watchedVideos = listOf()

        val mainScope = MainScope()
        mainScope.launch {
            val videos = fetchVideos()
            setState {
                unwatchedVideos = videos
            }
        }
    }
}

external interface AppState : RState {
    var currentVideo: MediaDomain?
    var unwatchedVideos: List<MediaDomain>
    var watchedVideos: List<MediaDomain>
}

suspend fun fetchVideo(id: Int): MediaDomain {
    val response = window
        .fetch("https://my-json-server.typicode.com/kotlin-hands-on/kotlinconf-json/videos/$id")
        .await()
        .json()
        .await()
    return (response as Video).let {
        MediaDomain(
            id = it.id.toLong(),
            url = it.videoUrl,
            title = it.title,
            channelData = ChannelDomain(
                title = it.speaker,
                platform = PlatformDomain.YOUTUBE,
                platformId = ""
            ),
            platformId = it.videoUrl.substring(it.videoUrl.lastIndexOf("=") + 1),
            platform = PlatformDomain.YOUTUBE,
            mediaType = MediaDomain.MediaTypeDomain.VIDEO
        )
    }
}

suspend fun fetchVideos(): List<MediaDomain> = coroutineScope {
    (1..25).map { id ->
        async {
            fetchVideo(id)
        }
    }.awaitAll()
}

