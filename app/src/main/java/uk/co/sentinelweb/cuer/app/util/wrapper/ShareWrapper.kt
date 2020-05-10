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
                |by "${media.channelTitle}" (${YoutubeJavaApiWrapper.channelUrl(media)})
                | 
                |Sent via Cuer @cuerapp (https://twitter.com/cuerapp) 
                |
                """.trimMargin()
            )
            putExtra(Intent.EXTRA_SUBJECT, "Watch '${media.title}' by '${media.channelTitle}'")
            type = "text/plain"
        }.let {
            Intent.createChooser(it, "Share Video: ${media.title}")
        }.run {
            activity.startActivity(this)
        }
    }
}