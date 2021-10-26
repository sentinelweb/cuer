package uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings

import com.russhwolf.settings.Settings

class MultiPlatformPreferencesWrapperImpl constructor(
) : MultiPlatformPreferencesWrapper {

    private val prefs: Settings by lazy {
        Settings()
    }

    override fun getLong(field: MultiPlatformPrefences, def: Long): Long = prefs.getLong(field.fname, def)

    override fun getLong(field: MultiPlatformPrefences): Long? =
        if (prefs.hasKey(field.fname)) prefs.getLong(field.fname, 0)
        else null

    override fun putLong(field: MultiPlatformPrefences, value: Long) {
        prefs.putLong(field.fname, value)
    }

    override fun getInt(field: MultiPlatformPrefences, def: Int): Int =
        prefs.getInt(field.fname, def)

    override fun getInt(field: MultiPlatformPrefences): Int? =
        if (prefs.hasKey(field.fname)) prefs.getInt(field.fname, 0)
        else null

    override fun putInt(field: MultiPlatformPrefences, value: Int) {
        prefs.putInt(field.fname, value)
    }

    override fun getString(field: MultiPlatformPrefences, def: String?): String? =
        prefs.getStringOrNull(field.fname) ?: def

    override fun putString(field: MultiPlatformPrefences, value: String) {
        prefs.putString(field.fname, value)
    }

    // todo use serialization to implement enum
    override fun putEnum(field: MultiPlatformPrefences, value: Enum<*>) {
        throw UnsupportedOperationException("need to implement enum")
        //prefs.putString(field.fname, value.toString())
    }

    override fun <E : Enum<E>> getEnum(field: MultiPlatformPrefences, def: E): E =
        throw UnsupportedOperationException("need to implement enum")
//        getString(field, null)
//            ?.let { pref -> def::class.java.enumConstants.find { it.name == pref } }
//            ?: def

    private fun putEnum(fieldName: String, value: Enum<*>) {
        throw UnsupportedOperationException("need to implement enum")
        //prefs.putString(fieldName, value.toString())
    }

    private fun getEnum(fieldName: String, def: Enum<*>): Enum<*> =
        throw UnsupportedOperationException("need to implement enum")
//        prefs.getStringOrNull(fieldName)
//            ?.let { pref -> def::class.java.enumConstants.find { it.name == pref } }
//            ?: def

    override fun getBoolean(field: MultiPlatformPrefences, def: Boolean): Boolean =
        prefs.getBoolean(field.fname, def)

    override fun putBoolean(field: MultiPlatformPrefences, value: Boolean) {
        prefs.putBoolean(field.fname, value)
    }

    override fun remove(field: MultiPlatformPrefences) {
        prefs.remove(field.fname)
        prefs.remove(field.fname + PAIR_FIRST)
        prefs.remove(field.fname + PAIR_SECOND)
    }

    override fun has(field: MultiPlatformPrefences): Boolean = prefs.hasKey(field.fname) || prefs.hasKey(field.fname + PAIR_FIRST)

    override fun <T1 : Any?, T2 : Any?> putPair(field: MultiPlatformPrefences, pair: Pair<T1, T2>) {
        putValue(field.fname + PAIR_FIRST, pair.first)
        putValue(field.fname + PAIR_SECOND, pair.second)
    }

    override fun <T1 : Any?, T2 : Any?> getPair(field: MultiPlatformPrefences, def: Pair<T1, T2>): Pair<T1, T2> =
        getVal(field.fname + PAIR_FIRST, def.first) to
                getVal(field.fname + PAIR_SECOND, def.second)

    override fun <T1 : Any?, T2 : Any?> getPairNonNull(field: MultiPlatformPrefences, def: Pair<T1, T2>): Pair<T1, T2>? =
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
            else -> throw Exception("Type not supported: ${def?.let { it::class }}")
        }

    private fun putValue(fieldName: String, value: Any?) {
        when (value) {
            null -> prefs.remove(fieldName)
            is Boolean -> prefs.putBoolean(fieldName, value)
            is Long -> prefs.putLong(fieldName, value)
            is Int -> prefs.putInt(fieldName, value)
            is String -> prefs.putString(fieldName, value)
            is Enum<*> -> putEnum(fieldName, value)
            else -> throw Exception("Type not supported: ${value::class}")
        }
    }

    companion object {
        private val PAIR_FIRST = ".first"
        private val PAIR_SECOND = ".second"
    }

}