package uk.co.sentinelweb.cuer.app.util.wrapper

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import uk.co.sentinelweb.cuer.app.ui.common.dialog.appselect.AppDetails
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class AppListBuilder(
    private val activity: Context,
    private val log: LogWrapper
) {
    init {
        log.tag(this)
    }

    fun buildAppList(cryptoAppWhiteList: List<String>): List<AppDetails> =
        cryptoAppWhiteList
            .mapNotNull { isPackageInstalled(it, activity.packageManager) }
            .map { packageInfo ->
                AppDetails(
                    title = activity.packageManager.getApplicationLabel(packageInfo.applicationInfo),
                    appId = packageInfo.packageName,
                    icon = packageInfo.applicationInfo.loadIcon(activity.packageManager)
                )
            }
            .apply {
                forEach { appDetails -> log.d("${appDetails.title} - ${appDetails.appId}") }
            }

    private fun isPackageInstalled(
        packageName: String,
        packageManager: PackageManager
    ): PackageInfo? {
        return try {
            packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}