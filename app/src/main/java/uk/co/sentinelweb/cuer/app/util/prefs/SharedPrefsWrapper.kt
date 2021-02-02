package uk.co.sentinelweb.cuer.app.util.prefs

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import kotlin.reflect.KClass

interface Field {
    val fname: String
}

class SharedPrefsWrapper<T : Field> constructor(
    private val clazz: KClass<T>,
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

    fun getInt(field: T): Int? =
        if (prefs.contains(field.fname)) prefs.getInt(field.fname, 0)
        else null

    fun putInt(field: T, value: Int) {
        prefs.edit().putInt(field.fname, value).apply()
    }

    fun getString(field: T, def: String?): String? =
        prefs.getString(field.fname, def)

    fun putString(field: T, value: String) {
        prefs.edit().putString(field.fname, value).apply()
    }

    fun putEnum(field: T, value: Enum<*>) {
        prefs.edit().putString(field.fname, value.toString()).apply()
    }

    fun getEnum(field: T, def: Enum<*>): Enum<*> =
        getString(field, null)
            ?.let { pref -> def::class.java.enumConstants.find { it.name == pref } }
            ?: def


    fun getBoolean(field: T, def: Boolean): Boolean =
        prefs.getBoolean(field.fname, def)

    fun putBoolean(field: T, value: Boolean) {
        prefs.edit()
            .putBoolean(field.fname, value)
            .apply()
    }

    fun remove(field: T) {
        prefs.edit()
            .remove(field.fname)
            .remove(field.fname + PAIR_FIRST)
            .remove(field.fname + PAIR_SECOND)
            .apply()
    }

    fun has(field: T): Boolean = prefs.contains(field.fname) || prefs.contains(field.fname + PAIR_FIRST)

    fun <T1 : Any?, T2 : Any?> putPair(field: T, pair: Pair<T1, T2>) {
        putValue(field.fname + PAIR_FIRST, pair.first)
        putValue(field.fname + PAIR_SECOND, pair.second)
    }

    fun <T1 : Any?, T2 : Any?> getPair(field: T, def: Pair<T1, T2>): Pair<T1, T2> =
        getVal(field.fname + PAIR_FIRST, def.first) to
                getVal(field.fname + PAIR_SECOND, def.second)

    fun <T1 : Any?, T2 : Any?> getPairNonNull(field: T, def: Pair<T1, T2>): Pair<T1, T2>? =
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

// try with inline class
//    fun <E : Enum<E>> getEnum(field: T, def: E): E =
//        getEnum(prefs, field, def)

    //    inline fun <reified E : Enum<E>> getEnum(prefs:SharedPrefsWrapper<T>,field: T, def: E): E =
//        prefs.prefs.getString(field.fname, null)
//            ?.let { enumValueOf<E>(it) } ?: def
//
    companion object {
        private val PAIR_FIRST = ".first"
        private val PAIR_SECOND = ".second"
    }

}