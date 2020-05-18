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

    fun launchChannel(media: MediaDomain) =
        canLaunchChannel()
            .takeIf { it }
            .also {
                activity.startActivity(createChannelIntent(activity, media.channelData.id))
            } ?: false


    fun launchVideo(media: MediaDomain) =
        canLaunchVideo()
            .takeIf { it }
            ?.also {
                activity.startActivity(createPlayVideoIntent(activity, media.mediaId))
            } ?: false

    fun launchVideo(media: MediaDomain, forceFullScreen: Boolean, finishAfter: Boolean) =
        canLaunchVideoWithOptions()
            .takeIf { it }
            ?.also {
                activity.startActivity(
                    createPlayVideoIntentWithOptions(
                        activity,
                        media.mediaId,
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
        fun videoUrl(media: MediaDomain) = "https://www.youtube.com/watch?v=${media.mediaId}"
        fun videoShortUrl(media: MediaDomain) = "https://youtu.be/${media.mediaId}"
    }
}