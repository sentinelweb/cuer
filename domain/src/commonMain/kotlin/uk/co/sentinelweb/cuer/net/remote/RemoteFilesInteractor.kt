package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.domain.PlaylistAndChildrenDomain
import uk.co.sentinelweb.cuer.net.NetResult

interface RemoteFilesInteractor {

    suspend fun getFolderList(locator: Locator, path: String): NetResult<PlaylistAndChildrenDomain>
}