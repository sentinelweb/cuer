package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.google.android.youtube.player.YouTubeApiServiceUtil.isYouTubeApiServiceAvailable
import com.google.android.youtube.player.YouTubeInitializationResult.SUCCESS
import com.google.android.youtube.player.YouTubeIntents.*
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.platform.YoutubeUrl.Companion.videoUrl

class YoutubeJavaApiWrapper(
    private val activity: Activity,
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
            ?.let {
                activity.startActivity(createChannelIntent(activity, media.channelData.platformId))
                true
            } ?: false

    fun launchChannel(id: String) =
        canLaunchChannel()
            .takeIf { it }
            ?.let {
                activity.startActivity(createChannelIntent(activity, id))
                true
            } ?: false

    fun launchPlaylist(id: String) =
        canLaunchPlaylist()
            .takeIf { it }
            .also {
                activity.startActivity(createPlayPlaylistIntent(activity, id))
            } ?: false

    fun launchVideo(platformId: String) =
        canLaunchVideo()
            .takeIf { it }
            ?.also {
                activity.startActivity(createPlayVideoIntent(activity, platformId))
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

    fun launchVideoSystem(media: MediaDomain) = launchVideoSystem(media.platformId)

    fun launchVideoSystem(platformId: String): Boolean = runCatching {
        Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl(platformId)))
            .apply { activity.startActivity(this) }
    }.isSuccess

}