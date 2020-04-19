package com.roche.mdas.util.wrapper

import android.content.Context
import android.widget.Toast

class ToastWrapper(private val context: Context) {

    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}
