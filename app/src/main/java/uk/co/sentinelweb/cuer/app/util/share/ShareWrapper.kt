package uk.co.sentinelweb.cuer.app.util.share

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM
import uk.co.sentinelweb.cuer.domain.platform.YoutubeUrl.Companion.channelUrl
import uk.co.sentinelweb.cuer.domain.platform.YoutubeUrl.Companion.playlistUrl
import uk.co.sentinelweb.cuer.domain.platform.YoutubeUrl.Companion.videoShortUrl

class ShareWrapper(
    private val activity: AppCompatActivity
) {
    fun share(media: MediaDomain) {
        Intent().apply {
            action = Intent.ACTION_SEND
            data = Uri.parse(media.url)
            putExtra(
                Intent.EXTRA_TEXT,
                shortMessage(media).trimMargin()
            )
            putExtra(Intent.EXTRA_SUBJECT, "Watch '${media.title}' by '${media.channelData.title}'")
            type = "text/plain"
        }.let {
            Intent.createChooser(it, "Share Video: ${media.title}")
            //it // gives a different share sheet - a bit easier
        }.run {
            activity.startActivity(this)
        }
    }

    fun share(playlist: PlaylistDomain) {
        Intent().apply {
            action = Intent.ACTION_SEND
            data = Uri.parse(playlistUrl(playlist))
            putExtra(
                Intent.EXTRA_TEXT,
                playlistMessage(playlist).trimMargin()
            )
            val subject = "Shared playlist: '${playlist.title}'" + (playlist.channelData?.let { " by '${it.title}'" } ?: "")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            type = "text/plain"
        }.let {
            Intent.createChooser(it, "Share Playlist: ${playlist.title}")
        }.run {
            activity.startActivity(this)
        }
    }

    private fun fullMessage(media: MediaDomain): String {
        return """Yo cheez, Check out this vid:
                    |
                    |${media.title}
                    |${media.url}
                    |
                    |by "${media.channelData.title}" (${channelUrl(media)})
                    | 
                    |Sent via Cuer @cuerapp (https://twitter.com/cuerapp) 
                    |
                    """
    }

    private fun shortMessage(media: MediaDomain): String {
        return """${media.title}
                    |${media.url}
                    |
                    |by "${media.channelData.title}" (${channelUrl(media)})
                    """
    }

    private fun playlistMessage(playlist: PlaylistDomain): String {
        val sb = StringBuilder()
        sb.append("Playlist: ").append(playlist.title).append("\n\n")
        if (playlist.type == PLATFORM) {
            sb.append("URL:").append(playlistUrl(playlist)).append("\n\n")
        }
        playlist.config.description?.apply {
            sb.append(this).append("\n\n")
        }
        if (playlist.items.size > 0) {
            sb.append(Character.toChars(0x1F4FA)).append(" Videos:\n\n")
            playlist.items.forEach {
                it.media.apply {
                    if (starred) {
                        sb.append(Character.toChars(0x2B50)).append(" ")
                    }
                    sb.append(title).append("\n")
                    sb.append(videoShortUrl(this)).append("\n")
                    sb.append("   by: ").append(channelData.title).append(" - ")
                        .append(channelData.customUrl ?: channelUrl(channelData))
                        .append("\n\n")
                }
            }
        }

        sb.append("Sent via Cuer @cuerapp (https://twitter.com/cuerapp)").append("\n\n")
        return sb.toString()
    }

    fun getLinkFromIntent(intent: Intent): String? =
        intent.data?.toString()
            ?.takeIf { it.startsWith("http") }
            ?: intent.clipData
                ?.takeIf { it.itemCount > 0 }
                ?.let { it.getItemAt(0)?.let { it.uri ?: it.text }.toString() }
                ?.takeIf { it.startsWith("http") }
            ?: intent.getStringExtra(Intent.EXTRA_TEXT)
                ?.takeIf { it.startsWith("http") }

    fun getTextFromIntent(intent: Intent): String? =
        intent.data?.toString()
            ?: intent.clipData
                ?.takeIf { it.itemCount > 0 }
                ?.let { it.getItemAt(0)?.let { it.uri ?: it.text }.toString() }
            ?: intent.getStringExtra(Intent.EXTRA_TEXT)

}