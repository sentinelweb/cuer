package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPrefences
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.CategoryDomain

class BrowseRecentCategories constructor(
    private val prefs: MultiPlatformPreferencesWrapper,
    private val log: LogWrapper,
) {
    init {
        log.tag(this)
    }

    fun getRecent() = (prefs.getString(MultiPlatformPrefences.BROWSE_RECENT_TITLES, null)
        ?.split(",")?.toMutableList()
        ?.apply { log.d("recent: " + toString()) }
        ?: mutableListOf())

    fun addRecent(cat: CategoryDomain) {
        val current = getRecent()
        log.d("add recent: " + cat.title)
        current.remove(cat.title)
        current.add(cat.title)
        while (current.size > MAX_RECENT) current.removeAt(0)
        prefs.putString(MultiPlatformPrefences.BROWSE_RECENT_TITLES, current.toTypedArray().joinToString(","))
    }

    companion object {
        private const val MAX_RECENT = 5
    }
}