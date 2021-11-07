package uk.co.sentinelweb.cuer.app.util.recent

import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPrefences.RECENT_PLAYLISTS
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class RecentLocalPlaylists constructor(
    private val prefs: MultiPlatformPreferencesWrapper,
    private val log: LogWrapper,
) {
    init {
        log.tag(this)
    }

    fun getRecent(): List<Long> = (prefs.getString(RECENT_PLAYLISTS, null)
        ?.split(",")
        ?.map { it.toLong() }
        ?.toMutableList()
        ?: mutableListOf())

    fun addRecent(pl: PlaylistDomain) {
        val current = getRecent().toMutableList()
        if (pl.id != null) {
            current.remove(pl.id)
            current.add(pl.id)
            while (current.size > MAX_RECENT) current.removeAt(0)
            prefs.putString(RECENT_PLAYLISTS, current.toTypedArray().joinToString(","))
        }
    }

    companion object {
        private const val MAX_RECENT = 15
    }
}