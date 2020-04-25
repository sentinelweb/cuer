package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import androidx.appcompat.app.AlertDialog

class AlertDialogWrapper constructor(val activity: Activity) {

    fun showMessage(title: String, text: String, okTapped: () -> Unit = {}): Unit = AlertDialog
        .Builder(activity)
        .setTitle(title)
        .setMessage(text)
        .setPositiveButton("OK") { _, _ -> okTapped.invoke() }
        .show()
        .let { Unit }
}