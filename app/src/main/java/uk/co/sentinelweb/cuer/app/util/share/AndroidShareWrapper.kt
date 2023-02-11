package uk.co.sentinelweb.cuer.app.util.share

import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import uk.co.sentinelweb.cuer.app.usecase.ShareUseCase
import uk.co.sentinelweb.cuer.app.util.link.YoutubeUrl.Companion.playlistUrl
import uk.co.sentinelweb.cuer.app.util.wrapper.ShareWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class AndroidShareWrapper(
    private val activity: AppCompatActivity
) : ShareWrapper() {

    override fun share(media: MediaDomain) {
        Intent().apply {
            action = ACTION_SEND
            data = Uri.parse(media.url)
            putExtra(
                EXTRA_TEXT,
                fullMessage(media).trimMargin()
            )
            putExtra(EXTRA_SUBJECT, "Watch '${media.title}' by '${media.channelData.title}'")
            type = "text/plain"
        }.let {
            Intent.createChooser(it, "Share Video: ${media.title}")
            //it // gives a different share sheet - a bit easier
        }.run {
            activity.startActivity(this)
        }
    }

    override fun share(playlist: PlaylistDomain) {
        Intent().apply {
            action = ACTION_SEND
            data = Uri.parse(playlistUrl(playlist))
            putExtra(
                EXTRA_TEXT,
                playlistMessage(playlist).trimMargin()
            )
            val subject =
                "Shared playlist: '${playlist.title}'" + (playlist.channelData?.let { " by '${it.title}'" } ?: "")
            putExtra(EXTRA_SUBJECT, subject)
            type = "text/plain"
        }.let {
            Intent.createChooser(it, "Share Playlist: ${playlist.title}")
        }.run {
            activity.startActivity(this)
        }
    }

    fun share(shareData: ShareUseCase.Data) {
        Intent().apply {
            action = ACTION_SEND
            data = Uri.parse(shareData.uri)
            putExtra(EXTRA_TEXT, shareData.text)
            putExtra(EXTRA_SUBJECT, shareData.subject)
            type = "text/plain"
        }
            .let { createChooser(it, shareData.title) }
            .run { activity.startActivity(this) }
    }

    fun getLinkFromIntent(intent: Intent): String? =
        intent.data?.toString()
            ?.takeIf { it.startsWith("http") }
            ?: intent.clipData
                ?.takeIf { it.itemCount > 0 }
                ?.let { it.getItemAt(0)?.let { it.uri ?: it.text }.toString() }
                ?.takeIf { it.startsWith("http") }
            ?: intent.getStringExtra(EXTRA_TEXT)
                ?.takeIf { it.startsWith("http") }

    fun getTextFromIntent(intent: Intent): String? =
        intent.data?.toString()
            ?: intent.clipData
                ?.takeIf { it.itemCount > 0 }
                ?.let { it.getItemAt(0)?.let { it.uri ?: it.text }.toString() }
            ?: intent.getStringExtra(EXTRA_TEXT)

    override fun open(url: String) {
        Intent().apply {
            action = ACTION_VIEW
            data = Uri.parse(url)
        }
            .run { activity.startActivity(this) }
    }
}