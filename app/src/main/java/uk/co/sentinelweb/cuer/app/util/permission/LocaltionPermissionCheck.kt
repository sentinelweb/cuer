package uk.co.sentinelweb.cuer.app.util.permission

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog

class LocationPermissionOpener(private val a: Activity) : LocationPermissionLaunch {

    override fun launchLocationPermission() {
        val builder = AlertDialog.Builder(a)
        builder.setCancelable(true)
        // todo extract strings
        builder.setTitle("Location Permission")
        builder.setMessage("To get the SSID (used for the remote autostart). This is ONLY needed if you want the remote server to start on certain WiFi networks.")
        builder.setPositiveButton("Open Settings") { dialog, which -> // The app will redirect to the 'Display over other apps' in Settings.
            val intent = Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS,
                Uri.parse("package:" + a.getPackageName())
            )
            a.startActivityForResult(intent, Activity.RESULT_OK)
        }
        val dialog = builder.create()
        dialog.show()
    }
}