package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import android.view.View
import com.google.android.material.snackbar.Snackbar

interface SnackbarWrapper {
    fun make(
        msg: String,
        length: Int = Snackbar.LENGTH_SHORT,
        actionText: String? = null,
        action: ((View) -> Unit)? = null
    ): Snackbar
}

class AndroidSnackbarWrapper constructor(private val a: Activity) : SnackbarWrapper {

    override fun make(
        msg: String,
        length: Int,
        actionText: String?,
        action: ((View) -> Unit)?
    ) = Snackbar.make(a.findViewById(android.R.id.content), msg, length).apply {
        if (actionText != null) setAction(actionText, action)
    }

}