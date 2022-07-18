package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import uk.co.sentinelweb.cuer.domain.AppDetailsDomain


class AppLauncherWrapper(private val activity: Activity) {
    fun launchApp(app: AppDetailsDomain): Boolean =
        activity.packageManager
            .getLaunchIntentForPackage(app.appId)
            ?.run { activity.startActivity(this); true }
            ?: app.run { throw ActivityNotFoundException("for $appId : $title") }
}