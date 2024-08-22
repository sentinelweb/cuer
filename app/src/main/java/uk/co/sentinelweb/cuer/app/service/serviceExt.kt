package uk.co.sentinelweb.cuer.app.service

import android.content.Intent

const val EXTRA_ITEM_ID: String = "cuer:itemId"

fun Intent.extrasToMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    val extras = extras
    if (extras != null) {
        for (key in extras.keySet()) {
            val value = extras.get(key)
            map[key] = value as Any
        }
    }
    return map
}
