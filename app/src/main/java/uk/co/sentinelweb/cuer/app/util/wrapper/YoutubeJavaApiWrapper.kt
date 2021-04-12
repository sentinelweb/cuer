package uk.co.sentinelweb.cuer.app.util.wrapper

import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import com.google.android.youtube.player.YouTubeApiServiceUtil.isYouTubeApiServiceAvailable
import com.google.android.youtube.player.YouTubeInitializationResult.SUCCESS
import com.google.android.youtube.player.YouTubeIntents.*
import uk.co.sentinelweb.cuer.domain.MediaDomain

class YoutubeJavaApiWrapper(
    private val activity: AppCompatActivity
) {

    @VisibleForTesting
    internal fun canLaunchChannel() = canResolveChannelIntent(activity)

    @VisibleForTesting
    internal fun canLaunchVideo() = canResolvePlayVideoIntent(activity)

    @VisibleForTesting
    internal fun canLaunchVideoWithOptions() = canResolvePlayVideoIntentWithOptions(activity)

    @VisibleForTesting
    internal fun canLaunchPlaylist() = canResolvePlayPlaylistIntent(activity)

    fun launchChannel(media: MediaDomain) =
        canLaunchChannel()
            .takeIf { it }
            .also {
                activity.startActivity(createChannelIntent(activity, media.channelData.platformId))
            } ?: false

    fun launchChannel(id: String) =
        canLaunchChannel()
            .takeIf { it }
            .also {
                activity.startActivity(createChannelIntent(activity, id))
            } ?: false


    fun launchPlaylist(id: String) =
        canLaunchPlaylist()
            .takeIf { it }
            .also {
                activity.startActivity(createPlayPlaylistIntent(activity, id))
            } ?: false


    fun launchVideo(media: MediaDomain) =
        canLaunchVideo()
            .takeIf { it }
            ?.also {
                activity.startActivity(createPlayVideoIntent(activity, media.platformId))
            } ?: false

    fun launchVideo(media: MediaDomain, forceFullScreen: Boolean, finishAfter: Boolean) =
        canLaunchVideoWithOptions()
            .takeIf { it }
            ?.also {
                activity.startActivity(
                    createPlayVideoIntentWithOptions(
                        activity,
                        media.platformId,
                        forceFullScreen,
                        finishAfter
                    )
                )
            } ?: launchVideo(media)

    fun isApiAvailable() = when (isYouTubeApiServiceAvailable(activity)) {
        SUCCESS -> true
        else -> {
            false
        }
    }

    companion object {
        fun channelUrl(media: MediaDomain) =
            "https://www.youtube.com/channel/${media.channelData.id}"

        fun videoUrl(media: MediaDomain) = "https://www.youtube.com/watch?v=${media.platformId}"
        fun videoShortUrl(media: MediaDomain) = "https://youtu.be/${media.platformId}"
    }
}