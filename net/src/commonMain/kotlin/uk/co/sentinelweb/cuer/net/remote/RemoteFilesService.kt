package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.domain.system.ResponseDomain
import uk.co.sentinelweb.cuer.net.client.ServiceExecutor
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.FOLDER_LIST_API.PATH
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.FOLDER_LIST_API.P_PARAM
import uk.co.sentinelweb.cuer.remote.server.ipport

internal class RemoteFilesService(
    private val executor: ServiceExecutor
) {

    internal suspend fun execFolder(locator: Locator, remoteFolderPath: String): ResponseDomain =
        executor.getResponse(
            path = locator.ipport() + PATH,
            urlParams = mapOf(P_PARAM to remoteFolderPath)
        )
}