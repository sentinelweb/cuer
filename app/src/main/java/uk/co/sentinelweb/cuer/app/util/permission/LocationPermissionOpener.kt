package uk.co.sentinelweb.cuer.app.util.permission

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class LocationPermissionOpener(
    private val activity: Activity,
    private val log: LogWrapper
) : LocationPermissionLaunch {

    init {
        log.tag(this)
    }
    override fun launchLocationPermission() {
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(true)
        builder.setTitle("Location Permission")
        builder.setMessage("To get the SSID (used for the remote autostart). This is ONLY needed if you want the remote server to start on certain WiFi networks.")
        builder.setPositiveButton("Open Settings") { dialog, which -> // The app will redirect to the 'Display over other apps' in Settings.
            openLocationSettings()
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun openLocationSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }

}
