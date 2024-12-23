package uk.co.sentinelweb.cuer.app.util.prefs

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class SharedPrefsWrapper constructor(
    private val app: Application,
    private val log: LogWrapper
) : GeneralPreferencesWrapper {

    private val prefs: SharedPreferences by lazy {
        app.getSharedPreferences(GeneralPreferences::class.simpleName, 0)
    }

    override fun getLong(field: GeneralPreferences, def: Long): Long = prefs.getLong(field.fname, def)

    override fun getLong(field: GeneralPreferences): Long? =
        if (prefs.contains(field.fname)) prefs.getLong(field.fname, 0)
        else null

    @SuppressLint("ApplySharedPref")
    override fun putLong(field: GeneralPreferences, value: Long) {
        prefs.edit().putLong(field.fname, value).commit()
    }

    override fun getInt(field: GeneralPreferences): Int? =
        if (prefs.contains(field.fname)) prefs.getInt(field.fname, 0)
        else null

    override fun getInt(field: GeneralPreferences, def: Int): Int =
        prefs.getInt(field.fname, def)

    override fun putInt(field: GeneralPreferences, value: Int) {
        prefs.edit().putInt(field.fname, value).apply()
    }

    override fun getFloat(field: GeneralPreferences): Float? =
        if (prefs.contains(field.fname)) prefs.getFloat(field.fname, 0f)
        else null

    override fun getFloat(field: GeneralPreferences, def: Float): Float =
        prefs.getFloat(field.fname, def)

    override fun putFloat(field: GeneralPreferences, value: Float) {
        prefs.edit().putFloat(field.fname, value).apply()
    }

    override fun getString(field: GeneralPreferences, def: String?): String? =
        prefs.getString(field.fname, def)

    override fun putString(field: GeneralPreferences, value: String) {
        prefs.edit().putString(field.fname, value).apply()
    }

    override fun putEnum(field: GeneralPreferences, value: Enum<*>) {
        prefs.edit().putString(field.fname, value.toString()).apply()
    }

    override fun <E : Enum<E>> getEnum(field: GeneralPreferences, def: E): E =
        getString(field, null)
            ?.let { pref -> def::class.java.enumConstants.find { it.name == pref } }
            ?: def

    override fun getBoolean(field: GeneralPreferences, def: Boolean): Boolean =
        prefs.getBoolean(field.fname, def)

    override fun getBoolean(field: MultiPlatformPreferences, ext: String, def: Boolean): Boolean =
        prefs.getBoolean(field.fname + ext, def)

    override fun putBoolean(field: GeneralPreferences, value: Boolean) {
        prefs.edit()
            .putBoolean(field.fname, value)
            .apply()
    }

    override fun putBoolean(field: MultiPlatformPreferences, ext: String, value: Boolean) {
        prefs.edit().putBoolean(field.fname + ext, value).apply()
    }

    override fun remove(field: GeneralPreferences) {
        prefs.edit()
            .remove(field.fname)
            .remove(field.fname + PAIR_FIRST)
            .remove(field.fname + PAIR_SECOND)
            .apply()
    }

    override fun has(field: GeneralPreferences): Boolean = prefs.contains(field.fname) || prefs.contains(field.fname + PAIR_FIRST)

    override fun <T1 : Any?, T2 : Any?> putPair(field: GeneralPreferences, pair: Pair<T1, T2>) {
        putValue(field.fname + PAIR_FIRST, pair.first)
        putValue(field.fname + PAIR_SECOND, pair.second)
    }

    override fun <T1 : Any?, T2 : Any?> getPair(field: GeneralPreferences, def: Pair<T1, T2>): Pair<T1, T2> =
        getVal(field.fname + PAIR_FIRST, def.first) to
                getVal(field.fname + PAIR_SECOND, def.second)

    override fun <T1 : Any?, T2 : Any?> getPairNonNull(field: GeneralPreferences, def: Pair<T1, T2>): Pair<T1, T2>? =
        if (has(field))
            getPair(field, def)
                .takeIf { it.first != null && it.second != null }
        else null

    @Suppress("UNCHECKED_CAST")
    private fun <V : Any?> getVal(fieldName: String, def: V): V =
        when (def) {
            is Boolean -> prefs.getBoolean(fieldName, def) as V
            is Long -> prefs.getLong(fieldName, def) as V
            is Int -> prefs.getInt(fieldName, def) as V
            is String -> prefs.getString(fieldName, def) as V
            is Enum<*> -> getEnum(fieldName, def as Enum<*>) as V
            else -> throw Exception("Type not supported: ${def?.let { it::class.java.name }}")
        }

    private fun putValue(fieldName: String, value: Any?) {
        when (value) {
            null -> prefs.edit().remove(fieldName).apply()
            is Boolean -> prefs.edit().putBoolean(fieldName, value).apply()
            is Long -> prefs.edit().putLong(fieldName, value).apply()
            is Int -> prefs.edit().putInt(fieldName, value).apply()
            is String -> prefs.edit().putString(fieldName, value).apply()
            is Enum<*> -> putEnum(fieldName, value)
            else -> throw Exception("Type not supported: ${value::class.java.name}")
        }
    }

    private fun putEnum(fieldName: String, value: Enum<*>) {
        prefs.edit().putString(fieldName, value.toString()).apply()
    }

    private fun getEnum(fieldName: String, def: Enum<*>): Enum<*> =
        prefs.getString(fieldName, null)
            ?.let { pref -> def::class.java.enumConstants.find { it.name == pref } }
            ?: def

    companion object {
        private val PAIR_FIRST = ".first"
        private val PAIR_SECOND = ".second"
    }


}