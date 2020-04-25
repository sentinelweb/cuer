package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import com.google.android.material.snackbar.Snackbar

class SnackbarWrapper constructor(private val a: Activity) {

    fun show(msg: String, length: Int = Snackbar.LENGTH_SHORT) =
        Snackbar.make(a.findViewById(android.R.id.content), msg, length)
}