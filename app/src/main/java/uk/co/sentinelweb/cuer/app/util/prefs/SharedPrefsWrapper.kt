package uk.co.sentinelweb.cuer.app.util.prefs

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

interface Field {
    val fname: String
}

class SharedPrefsWrapper<T : Field> constructor(
    private val clazz: Class<T>,
    private val app: Application,
    private val log: LogWrapper
) {
    private val prefs: SharedPreferences by lazy {
        app.getSharedPreferences(clazz.simpleName, 0)
    }

    fun getLong(field: T, def: Long): Long = prefs.getLong(field.fname, def)

    fun getLong(field: T): Long? =
        if (prefs.contains(field.fname)) prefs.getLong(field.fname, 0)
        else null

    @SuppressLint("ApplySharedPref")
    fun putLong(field: T, value: Long) {
        prefs.edit().putLong(field.fname, value).commit()
    }

    fun getInt(field: T, def: Int): Int =
        prefs.getInt(field.fname, def)


    fun putInt(field: T, value: Int) {
        prefs.edit().putInt(field.fname, value).apply()
    }

    fun getString(field: T, def: String): String? =
        prefs.getString(field.fname, def)


    fun putString(field: T, value: String) {
        prefs.edit().putString(field.fname, value).apply()
    }

    fun getBoolean(field: T, def: Boolean): Boolean =
        prefs.getBoolean(field.fname, def)


    fun putBoolean(field: T, value: Boolean) {
        prefs.edit().putBoolean(field.fname, value).apply()
    }

    fun remove(field: T) {
        prefs.edit().remove(field.fname).apply()
    }
}