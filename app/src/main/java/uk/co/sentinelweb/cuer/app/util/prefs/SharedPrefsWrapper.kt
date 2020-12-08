package uk.co.sentinelweb.cuer.app.util.prefs

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

interface Field {
    val fname: String
}

class SharedPrefsWrapper<T : Field> constructor(
    clazz: Class<T>,
    app: Application
) {
    private val prefs: SharedPreferences

    init {
        prefs = app.getSharedPreferences(clazz.simpleName, MODE_PRIVATE)
    }

    fun getLong(field: T, def: Long): Long = prefs.getLong(field.fname, def)

    fun getLong(field: T): Long? =
        if (prefs.contains(field.fname)) prefs.getLong(field.fname, 0)
        else null

    fun putLong(value: Long, field: T) {
        prefs.edit().putLong(field.fname, value).apply()
    }

    fun getInt(field: T, def: Int) {
        prefs.getInt(field.fname, def)
    }

    fun putInt(value: Int, field: T) {
        prefs.edit().putInt(field.fname, value).apply()
    }

    fun getString(field: T, def: String) {
        prefs.getString(field.fname, def)
    }

    fun putString(value: String, field: T) {
        prefs.edit().putString(field.fname, value).apply()
    }

    fun getBoolean(field: T, def: Boolean) {
        prefs.getBoolean(field.fname, def)
    }

    fun putBoolean(value: Boolean, field: T) {
        prefs.edit().putBoolean(field.fname, value).apply()
    }
}