package uk.co.sentinelweb.cuer.db.util

import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper

// used for testing DatabaseFactory
class MultiPlatformPreferencesWrapperDbTestImpl : MultiPlatformPreferencesWrapper {
    private val map: MutableMap<MultiPlatformPreferences, Any> = mutableMapOf()
    override fun getLong(field: MultiPlatformPreferences, def: Long): Long {
        TODO("Not yet implemented")
    }

    override fun getLong(field: MultiPlatformPreferences): Long? {
        TODO("Not yet implemented")
    }

    override fun putLong(field: MultiPlatformPreferences, value: Long) {
        TODO("Not yet implemented")
    }

    override fun getInt(field: MultiPlatformPreferences, def: Int): Int =
        map.get(field) as? Int ?: def


    override fun getInt(field: MultiPlatformPreferences): Int? =
        map.get(field) as? Int

    override fun putInt(field: MultiPlatformPreferences, value: Int) {
        map.put(field, value)
    }

    override fun getString(field: MultiPlatformPreferences, def: String?): String? {
        TODO("Not yet implemented")
    }

    override fun putString(field: MultiPlatformPreferences, value: String) {
        TODO("Not yet implemented")
    }

    override fun putEnum(field: MultiPlatformPreferences, value: Enum<*>) {
        TODO("Not yet implemented")
    }

    override fun <E : Enum<E>> getEnum(field: MultiPlatformPreferences, def: E): E {
        TODO("Not yet implemented")
    }

    override fun getBoolean(field: MultiPlatformPreferences, def: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun getBoolean(field: MultiPlatformPreferences, ext: String, def: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun putBoolean(field: MultiPlatformPreferences, value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun putBoolean(field: MultiPlatformPreferences, ext: String, value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun remove(field: MultiPlatformPreferences) {
        TODO("Not yet implemented")
    }

    override fun has(field: MultiPlatformPreferences): Boolean {
        TODO("Not yet implemented")
    }

    override fun <T1, T2> putPair(field: MultiPlatformPreferences, pair: Pair<T1, T2>) {
        TODO("Not yet implemented")
    }

    override fun <T1, T2> getPair(field: MultiPlatformPreferences, def: Pair<T1, T2>): Pair<T1, T2> {
        TODO("Not yet implemented")
    }

    override fun <T1, T2> getPairNonNull(field: MultiPlatformPreferences, def: Pair<T1, T2>): Pair<T1, T2>? {
        TODO("Not yet implemented")
    }

}