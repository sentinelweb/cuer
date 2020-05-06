package uk.co.sentinelweb.cuer.app.util.wrapper

import androidx.appcompat.app.AppCompatActivity
import com.google.android.youtube.player.YouTubeIntents
import uk.co.sentinelweb.cuer.domain.MediaDomain

class YoutubeApiWrapper(
    private val activity: AppCompatActivity
) {
    fun canLaunchChannel() = YouTubeIntents.canResolveChannelIntent(activity)
    fun canLaunchVideo() = YouTubeIntents.canResolveChannelIntent(activity)

    fun launchChannel(media: MediaDomain) =
        activity.startActivity(YouTubeIntents.createChannelIntent(activity, media.channelId))

    fun launchVideo(media: MediaDomain) =
        activity.startActivity(YouTubeIntents.createPlayVideoIntent(activity, media.mediaId))
}