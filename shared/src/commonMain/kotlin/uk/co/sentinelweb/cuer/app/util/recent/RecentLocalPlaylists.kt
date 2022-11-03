package uk.co.sentinelweb.cuer.app.util.recent

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class RecentLocalPlaylists constructor(
    private val prefs: MultiPlatformPreferencesWrapper,
    private val log: LogWrapper,
//    private val localSearch: LocalSearchPlayistInteractor,
) {
    init {
        log.tag(this)
    }

    fun getRecent(): List<Long> = prefs.recentIds
        ?.split(",")
        ?.map { it.toLong() }
        ?.toMutableList()
        ?: mutableListOf()

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
            prefs.recentIds = current.toTypedArray().joinToString(",")
        }
    }

    fun buildRecentSelectionList(): List<Identifier<Long>> {
        val recent = mutableListOf<Identifier<Long>>()

//        prefs.lastAddedPlaylistId
//            ?.toIdentifier(LOCAL)
//            ?.also { recent.add(it) }

        prefs.pinnedPlaylistId
            ?.toIdentifier(LOCAL)
            ?.also { recent.add(0, it) }

//        localSearch.search()
//            ?.playlists
//            ?.forEach { playlist ->
//                playlist.id?.toIdentifier(LOCAL)?.also { recent.add(it) }
//            }

        getRecent().reversed().forEach { id ->
            id.toIdentifier(LOCAL).also { recent.add(it) }
        }

        return recent.distinct()
    }

    companion object {
        private const val MAX_RECENT = 15
    }
}