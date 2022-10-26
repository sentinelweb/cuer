package uk.co.sentinelweb.cuer.app.util.recent

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.LocalSearchPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.RECENT_PLAYLISTS
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class RecentLocalPlaylists constructor(
    private val prefs: MultiPlatformPreferencesWrapper,
    private val log: LogWrapper,
    private val prefsWrapper: GeneralPreferencesWrapper,
    private val localSearch: LocalSearchPlayistInteractor,
) {
    init {
        log.tag(this)
    }

    fun getRecent(): List<Long> = (prefs.getString(RECENT_PLAYLISTS, null)
        ?.split(",")
        ?.map { it.toLong() }
        ?.toMutableList()
        ?.reversed()
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

    fun buildRecentSelectionList(): List<OrchestratorContract.Identifier<Long>> {
        val recent = mutableListOf<OrchestratorContract.Identifier<Long>>()
        prefsWrapper.getLong(GeneralPreferences.LAST_PLAYLIST_ADDED_TO)
            ?.toIdentifier(OrchestratorContract.Source.LOCAL)
            ?.also { recent.add(it) }
//        prefsWrapper.getLong(GeneralPreferences.LAST_PLAYLIST_CREATED)
//            ?.toIdentifier(OrchestratorContract.Source.LOCAL)
//            ?.also { recent.add(it) }
        prefsWrapper.getLong(GeneralPreferences.PINNED_PLAYLIST)
            ?.toIdentifier(OrchestratorContract.Source.LOCAL)
            ?.also { recent.add(0, it) }
        localSearch.search()
            ?.playlists
            ?.forEach { playlist ->
                playlist.id?.toIdentifier(OrchestratorContract.Source.LOCAL)?.also { recent.add(it) }
            }
        getRecent().forEach { id ->
            id.toIdentifier(OrchestratorContract.Source.LOCAL).also { recent.add(it) }
        }
        return recent.distinct()
    }

    companion object {
        private const val MAX_RECENT = 15
    }
}