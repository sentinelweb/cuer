package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog

class DisplayOverlayPermissionCheck() {
    private var dialog: AlertDialog? = null

    fun requestOverlayDisplayPermission(a: Activity) {
        val builder = AlertDialog.Builder(a)
        builder.setCancelable(true)
        builder.setTitle("Screen Overlay Permission Needed")
        builder.setMessage("Enable 'Display over other apps' from System Settings.")
        builder.setPositiveButton("Open Settings") { dialog, which -> //The app will redirect to the 'Display over other apps' in Settings.
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + a.getPackageName()))
            a.startActivityForResult(intent, Activity.RESULT_OK)
        }
        builder.setOnDismissListener { dialog = null }
        dialog = builder.create()
        dialog?.show()
    }

    fun checkOverlayDisplayPermission(a: Activity): Boolean { // todo check obsolete
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(a)) {
                false
            } else {
                true
            }
        } else {
            true
        }
    }
}