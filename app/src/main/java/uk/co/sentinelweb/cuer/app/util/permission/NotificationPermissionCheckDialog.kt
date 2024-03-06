package uk.co.sentinelweb.cuer.app.util.permission

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.provider.Settings

class NotificationPermissionCheckDialog(
    private val activity: Activity,
    private val notificationPermissionCheck: NotificationPermissionCheck
) {

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
            askPermission()
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun askPermission(channelId: String? = null) {
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
            channelId?.let { putExtra(Settings.EXTRA_CHANNEL_ID, it) }
        }
        activity.startActivity(intent)
    }
}