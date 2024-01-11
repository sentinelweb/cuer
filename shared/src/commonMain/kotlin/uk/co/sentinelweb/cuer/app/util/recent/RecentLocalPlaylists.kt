package uk.co.sentinelweb.cuer.app.util.recent

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.toGUID

class RecentLocalPlaylists constructor(
    private val prefs: MultiPlatformPreferencesWrapper,
    private val log: LogWrapper,
//    private val localSearch: LocalSearchPlayistInteractor,
) : RecentLocalPlaylistsContract {
    init {
        log.tag(this)
    }

    override fun getRecent(): List<GUID> = prefs.recentIds
        ?.split(",")
        ?.map { it.toGUID() }
        ?.toMutableList()
        ?: mutableListOf()

    override fun addRecent(pl: PlaylistDomain) {
        pl.id?.apply { addPlaylistId(id) }
    }

    override fun addRecentId(id: GUID) {
        addPlaylistId(id)
    }

    private fun addPlaylistId(id: GUID?) {
        val current = getRecent().toMutableList()
        if (id != null) {
            current.remove(id)
            current.add(id)
            while (current.size > MAX_RECENT) current.removeAt(0)
            prefs.recentIds = current.toTypedArray().map { it.value }.joinToString(",")
        }
    }

    override fun buildRecentSelectionList(): List<Identifier<GUID>> {
        val recent = mutableListOf<Identifier<GUID>>()

//        prefs.lastAddedPlaylistId
//            ?.toIdentifier(LOCAL)
//            ?.also { recent.add(it) }

        prefs.pinnedPlaylistId
            ?.also { recent.add(0, it.toIdentifier(LOCAL)) }

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