package uk.co.sentinelweb.cuer.app.util.wrapper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import uk.co.sentinelweb.cuer.domain.MediaDomain

class ShareWrapper(
    private val activity: AppCompatActivity
) {
    fun share(media: MediaDomain) {
        Intent().apply {
            action = Intent.ACTION_SEND
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
        }.run {
            activity.startActivity(this)
        }
    }

    // todo rewrite
    fun getLinkFromIntent(intent: Intent, callback: (String) -> Unit) {
        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.data?.host?.endsWith("youtube.com") ?: false) {
                    callback(intent.data.toString())
                } else if (intent.clipData?.itemCount ?: 0 > 0) {
                    callback(intent.clipData?.getItemAt(0)?.text.toString())
                } else {
                    intent.data?.let { callback(it.toString()) }
                }
            }
            Intent.ACTION_VIEW -> {
                if (intent.data?.host?.endsWith("youtube.com") ?: false) {
                    callback(intent.data.toString())
                } else {
                    intent.data?.let { callback(it.toString()) }
                }
            }
        }
    }
}