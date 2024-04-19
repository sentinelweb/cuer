package uk.co.sentinelweb.cuer.app.util.permission

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.provider.Settings
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class NotificationPermissionCheckDialog(
    private val activity: Activity,
    private val notificationPermissionCheck: NotificationPermissionCheck,
    private val log: LogWrapper
) {
    init {
        log.tag(this)
    }

    fun checkToShow() {
        if (!notificationPermissionCheck.isNotificationsEnabled()) {
            showEnableNotificationsDialog();
        }
    }

    // show an AlertDialog with 2 buttons and ask to enable
    private fun showEnableNotificationsDialog() {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
        alertDialogBuilder.setTitle("Enable notifications")
        alertDialogBuilder.setMessage("Do you want to enable notification?")
        alertDialogBuilder.setPositiveButton("Yes") { dialog, _ ->
            openNotificationSettings()
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun openNotificationSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS

        //for Android 5-7
        intent.putExtra("app_package", activity.packageName)
        intent.putExtra("app_uid", activity.applicationInfo.uid)

        // for Android 8 and above
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)

        activity.startActivity(intent)
    }
}