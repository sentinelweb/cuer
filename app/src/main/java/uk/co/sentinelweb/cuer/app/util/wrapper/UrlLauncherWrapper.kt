package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import com.google.android.youtube.player.YouTubeIntents
import uk.co.sentinelweb.cuer.domain.MediaDomain

class UrlLauncherWrapper(private val activity: Activity) {

    fun launchUrl(url: String): Boolean = try {
        val parse = Uri.parse(url)
        activity.startActivity(Intent(ACTION_VIEW, parse))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }

    fun launchWithChooser(url: String) = try {
        val parse = Uri.parse(url)
        activity.startActivity(
            Intent.createChooser(
                Intent(ACTION_VIEW, parse), "Launch ${parse.host}"
            )
        )
    } catch (e: ActivityNotFoundException) {
        false
    }
}
