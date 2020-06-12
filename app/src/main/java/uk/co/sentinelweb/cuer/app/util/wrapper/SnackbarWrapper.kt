package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import android.view.View
import com.google.android.material.snackbar.Snackbar

class SnackbarWrapper constructor(private val a: Activity) {

    fun make(
        msg: String,
        length: Int = Snackbar.LENGTH_SHORT,
        actionText: String? = null,
        action: ((View) -> Unit)? = null
    ) = Snackbar.make(a.findViewById(android.R.id.content), msg, length).apply {
        if (actionText != null) setAction(actionText, action)
    }

}