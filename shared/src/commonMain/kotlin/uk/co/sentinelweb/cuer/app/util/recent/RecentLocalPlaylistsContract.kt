package uk.co.sentinelweb.cuer.app.util.recent

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

interface RecentLocalPlaylistsContract {
    fun getRecent(): List<GUID>
    fun addRecent(pl: PlaylistDomain)
    fun addRecentId(id: GUID)
    fun buildRecentSelectionList(): List<OrchestratorContract.Identifier<GUID>>

}