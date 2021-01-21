package uk.co.sentinelweb.cuer.app.util.wrapper

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

class ToastWrapper(private val context: Context) {

    fun show(msg: String, dur: Int = Toast.LENGTH_LONG) {
        Toast.makeText(context, msg, dur).show()
    }

    fun show(@StringRes id: Int, dur: Int = Toast.LENGTH_LONG) {
        Toast.makeText(context, id, dur).show()
    }
}
