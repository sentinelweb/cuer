package uk.co.sentinelweb.cuer.app.util.wrapper

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AlertDialogWrapper constructor(val activity:AppCompatActivity) {

    fun showMessage(title: String, text: String, okTapped: () -> Unit = {}): Unit = AlertDialog
        .Builder(activity)
        .setTitle(title)
        .setMessage(text)
        .setPositiveButton("OK") { _, _ -> okTapped.invoke() }
        .show()
        .let { Unit }
}