package uk.co.sentinelweb.cuer.app.util.share

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain

class ShareWrapper(
    private val activity: AppCompatActivity
) {
    fun share(media: MediaDomain) {
        Intent().apply {
            action = Intent.ACTION_SEND
            data = Uri.parse(media.url)
            putExtra(
                Intent.EXTRA_TEXT,
                """Yo cheez, Check out this vid:
                |
                |${media.title}
                |${media.url}
                |
                |by "${media.channelData.title}" (${YoutubeJavaApiWrapper.channelUrl(media)})
                | 
                |Sent via Cuer @cuerapp (https://twitter.com/cuerapp) 
                |
                """.trimMargin()
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

    fun getLinkFromIntent(intent: Intent, callback: (String) -> Unit) {
        (intent.data?.toString()
            ?.takeIf { it.startsWith("http") }
            ?: intent.clipData
                ?.takeIf { it.itemCount > 0 }
                ?.let { it.getItemAt(0)?.let { it.uri ?: it.text }.toString() }
                ?.takeIf { it.startsWith("http") }
            ?: intent.getStringExtra(Intent.EXTRA_TEXT)
                ?.takeIf { it.startsWith("http") })
            ?.apply {
                callback(this)
            }
    }
}