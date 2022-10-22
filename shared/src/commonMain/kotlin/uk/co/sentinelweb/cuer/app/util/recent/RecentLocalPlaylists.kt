package uk.co.sentinelweb.cuer.app.util.recent

import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.RECENT_PLAYLISTS
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class RecentLocalPlaylists constructor(
    private val prefs: MultiPlatformPreferencesWrapper,
    @Suppress("CanBeParameter") private val log: LogWrapper,
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
        val id = pl.id
        addPlaylistId(id)
    }

    fun addRecentId(id: Long) {
        addPlaylistId(id)
    }

    private fun addPlaylistId(id: Long?) {
        val current = getRecent().toMutableList()
        if (id != null) {
            current.remove(id)
            current.add(id)
            while (current.size > MAX_RECENT) current.removeAt(0)
            prefs.putString(RECENT_PLAYLISTS, current.toTypedArray().joinToString(","))
        }
    }

    companion object {
        private const val MAX_RECENT = 15
    }
}