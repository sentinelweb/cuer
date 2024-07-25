package uk.co.sentinelweb.cuer.net.remote

import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.PlaylistAndChildrenDomain
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.net.client.RequestFailureException

internal class RemoteFilesKtorInteractor(
    private val remoteFilesService: RemoteFilesService,
    private val coroutines: CoroutineContextProvider
) : RemoteFilesInteractor {
    override suspend fun getFolderList(locator: Locator, path: String?): NetResult<PlaylistAndChildrenDomain> =
        withContext(coroutines.IO) {
            try {
                remoteFilesService.execFolder(locator, path)
                    .let { NetResult.Data(it.payload[0] as PlaylistAndChildrenDomain) }
            } catch (e: RequestFailureException) {
                NetResult.HttpError(e)
            } catch (e: Exception) {
                NetResult.Error(e)
            }
        }
}
