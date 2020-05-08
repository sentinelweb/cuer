package uk.co.sentinelweb.cuer.app.util.wrapper

import androidx.appcompat.app.AppCompatActivity
import com.google.android.youtube.player.YouTubeApiServiceUtil
import com.google.android.youtube.player.YouTubeInitializationResult.SUCCESS
import com.google.android.youtube.player.YouTubeIntents
import uk.co.sentinelweb.cuer.domain.MediaDomain

class YoutubeJavaApiWrapper(
    private val activity: AppCompatActivity
) {
    fun canLaunchChannel() = YouTubeIntents.canResolveChannelIntent(activity)

    fun canLaunchVideo() = YouTubeIntents.canResolveChannelIntent(activity)

    fun launchChannel(media: MediaDomain) =
        activity.startActivity(YouTubeIntents.createChannelIntent(activity, media.channelId))

    fun launchVideo(media: MediaDomain) =
        activity.startActivity(YouTubeIntents.createPlayVideoIntent(activity, media.mediaId))

    fun isApiAvailable() = when (YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(activity)) {
        SUCCESS -> true
        else -> {
            false
        }
    }

    companion object {
        fun channelUrl(media: MediaDomain) = "https://www.youtube.com/channel/${media.channelId}"
        fun videoUrl(media: MediaDomain) = "https://www.youtube.com/watch?v=${media.mediaId}"
        fun videoShortUrl(media: MediaDomain) = "https://youtu.be/${media.mediaId}"
    }
}