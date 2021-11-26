package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Activity
import android.view.View
import com.google.android.material.snackbar.Snackbar
import uk.co.sentinelweb.cuer.app.R

interface SnackbarWrapper {
    fun make(
        msg: String,
        length: Int = Snackbar.LENGTH_SHORT,
        actionText: String? = null,
        action: ((View) -> Unit)? = null
    ): Snackbar

    fun makeError(msg: String): Snackbar
}

class AndroidSnackbarWrapper constructor(
    private val a: Activity,
    private val res: ResourceWrapper
) : SnackbarWrapper {

    override fun make(
        msg: String,
        length: Int,
        actionText: String?,
        action: ((View) -> Unit)?
    ) = Snackbar.make(a.findViewById(android.R.id.content), msg, length).apply {
        if (actionText != null) setAction(actionText, action)
    }

    override fun makeError(msg: String): Snackbar =
        Snackbar.make(a.findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(res.getColor(R.color.error_snackbar))
            .setTextColor(res.getColor(R.color.text_primary))


}