package com.roche.mdas.util.wrapper

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

class SoftKeyboardWrapper {
    fun showSoftKeyboard(v: View) {
        v.apply {
            if (requestFocus()) {
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    fun hideSoftKeyboard(v: View) {
        v.apply {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(windowToken, 0)
        }
    }
}
