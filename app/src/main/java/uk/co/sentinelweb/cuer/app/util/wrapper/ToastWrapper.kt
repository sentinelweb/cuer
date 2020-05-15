package uk.co.sentinelweb.cuer.app.util.wrapper

import android.content.Context
import android.widget.Toast

class ToastWrapper(private val context: Context) {

    fun show(msg: String, dur: Int = Toast.LENGTH_LONG) {
        Toast.makeText(context, msg, dur).show()
    }
}
