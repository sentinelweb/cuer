package uk.co.sentinelweb.cuer.app.util.prefs

interface Field {
    val fname: String
}

interface PrefWrapper<T : Field> {
    fun getLong(field: T, def: Long): Long
    fun getLong(field: T): Long?
    fun putLong(field: T, value: Long)
    fun getInt(field: T, def: Int): Int
    fun getInt(field: T): Int?
    fun putInt(field: T, value: Int)
    fun getString(field: T, def: String?): String?
    fun putString(field: T, value: String)
    fun putEnum(field: T, value: Enum<*>)
    fun <E : Enum<E>> getEnum(field: T, def: E): E
    fun getBoolean(field: T, def: Boolean): Boolean
    fun putBoolean(field: T, value: Boolean)
    fun remove(field: T)
    fun has(field: T): Boolean
    fun <T1 : Any?, T2 : Any?> putPair(field: T, pair: Pair<T1, T2>)
    fun <T1 : Any?, T2 : Any?> getPair(field: T, def: Pair<T1, T2>): Pair<T1, T2>
    fun <T1 : Any?, T2 : Any?> getPairNonNull(field: T, def: Pair<T1, T2>): Pair<T1, T2>?
}

interface GeneralPreferencesWrapper : PrefWrapper<GeneralPreferences>