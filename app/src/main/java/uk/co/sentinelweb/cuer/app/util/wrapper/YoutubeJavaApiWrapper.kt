package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.google.android.youtube.player.YouTubeIntents.*
import uk.co.sentinelweb.cuer.app.util.link.YoutubeUrl.Companion.channelUrl
import uk.co.sentinelweb.cuer.app.util.link.YoutubeUrl.Companion.videoUrl
import uk.co.sentinelweb.cuer.app.util.link.YoutubeUrl.Companion.videoUrlWithTime
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.NO_PLATFORM_ID
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

@Suppress("TooManyFunctions")
class YoutubeJavaApiWrapper(
    private val activity: Activity,
    private val linkScanner: LinkScanner
) : PlatformLaunchWrapper {

    @VisibleForTesting
    internal fun canLaunchChannel() = canResolveChannelIntent(activity)

    @VisibleForTesting
    override fun canLaunchVideo() = canResolvePlayVideoIntent(activity)

    @VisibleForTesting
    override fun canLaunchVideoWithOptions() = canResolvePlayVideoIntentWithOptions(activity)

    private fun canLaunchPlaylist() = canResolvePlayPlaylistIntent(activity)

    override fun launchChannel(media: MediaDomain) =
        media.channelData.platformId
            ?.let { launchChannel(it) }
            ?: false

    // todo launch youtube app with customUrl and NoPlatformId
    @VisibleForTesting
    fun launchChannel(channel: ChannelDomain) =
        channel.let {
            if (it.platformId?.isNotEmpty() ?: false && it.platformId != NO_PLATFORM_ID) {
                return launchChannel(it.platformId!!)
            } else if (it.customUrl != null) {
                return launchChannelSystem(it)
            } else false
        }

    override fun launchChannel(id: String) = try {
        activity.startActivity(createChannelIntent(activity, id).newTask())
        true
    } catch (e: Exception) {
        false
    }

    private fun launchPlaylist(playlist: PlaylistDomain) =
        playlist.platformId
            ?.let { launchPlaylist(it) }
            ?: false

    override fun launchPlaylist(id: String) =
        canLaunchPlaylist()
            .takeIf { it }
            .also {
                activity.startActivity(createPlayPlaylistIntent(activity, id).newTask())
            } ?: false

    private fun launchVideo(platformId: String) =
        canLaunchVideo()
            .takeIf { it }
            ?.also {
                activity.startActivity(videoIntentFromApi(platformId))
            } ?: false

    override fun launchVideo(media: MediaDomain) =
        canLaunchVideo()
            .takeIf { it }
            ?.also {
                activity.startActivity(videoIntentFromApi(media.platformId))
            } ?: false

    private fun videoIntentFromApi(platformId: String): Intent =
        createPlayVideoIntent(activity, platformId)
            .newTask()

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
                    ).newTask()
                )
            } ?: launchVideo(media)

    override fun launchVideoSystem(platformId: String): Boolean = runCatching {
        Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl(platformId)))
            .newTask()
            .apply { activity.startActivity(this) }
    }.isSuccess

    override fun launchVideoWithTimeSystem(media: MediaDomain): Boolean = runCatching {
        Intent(Intent.ACTION_VIEW, Uri.parse(videoUrlWithTime(media)))
            .newTask()
            .apply { activity.startActivity(this) }
    }.isSuccess

    private fun launchChannelSystem(channel: ChannelDomain): Boolean = runCatching {
        Intent(Intent.ACTION_VIEW, Uri.parse(channelUrl(channel)))
            .newTask()
            .apply { activity.startActivity(this) }
    }.isSuccess

    override fun launch(address: String): Boolean =
        linkScanner.scan(address)
            ?.let {
                when (it.first) {
                    MEDIA -> launchVideo(it.second as MediaDomain)
                    PLAYLIST -> launchPlaylist((it.second as PlaylistDomain))
                    CHANNEL -> launchChannel((it.second as ChannelDomain))
                    else -> false
                }
            } ?: false

    private fun Intent.newTask() = setFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK)
}