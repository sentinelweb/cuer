package uk.co.sentinelweb.cuer.app.util.share

import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import uk.co.sentinelweb.cuer.app.usecase.ShareUseCase
import uk.co.sentinelweb.cuer.app.util.wrapper.ShareWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.platform.YoutubeUrl.Companion.channelUrl
import uk.co.sentinelweb.cuer.domain.platform.YoutubeUrl.Companion.playlistUrl
import uk.co.sentinelweb.cuer.domain.platform.YoutubeUrl.Companion.videoShortUrl

class AndroidShareWrapper(
    private val activity: AppCompatActivity
) : ShareWrapper {
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

    private fun fullMessage(media: MediaDomain): String {
        return """Hey! Check out this video:
                    |
                    |${media.title}
                    |${media.url}
                    |
                    |by "${media.channelData.title}" (${channelUrl(media)})
                    | 
                    |🌎 https://cuer.app
                    """.trimIndent()
    }

    private fun shortMessage(media: MediaDomain): String {
        return """${media.title}
                    |${media.url}
                    |
                    |by "${media.channelData.title}" (${channelUrl(media)})
                    """.trimIndent()
    }

    private fun playlistMessage(playlist: PlaylistDomain): String {
        val header = """
            Playlist: ${playlist.title}
            
            
        """.trimIndent()
        val url = playlist.platformId?.let {
            """
            URL: ${playlistUrl(playlist)}
            
            
            """.trimIndent()
        } ?: ""
        val desc = playlist.config.description?.let { "Description:\n $it\n\n" } ?: ""

        val items = if (playlist.items.size > 0) {
            "🎥 Videos:\n\n" +
                    playlist.items.joinToString("\n") {
                        it.media.let {
                            (if (it.starred) "⭐️ " else "") + """
                        ${it.title}
                        ${videoShortUrl(it)}
                           by: ${it.channelData.title} = ${it.channelData.customUrl ?: channelUrl(it.channelData)}
                        
                    """.trimIndent()
                        }
                    }
        } else ""
        val footer = "🌎 https://cuer.app"

        return header + url + desc + items + footer
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

}