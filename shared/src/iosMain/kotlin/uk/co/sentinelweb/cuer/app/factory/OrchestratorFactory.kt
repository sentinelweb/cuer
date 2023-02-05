package uk.co.sentinelweb.cuer.app.factory

import com.rickclephas.kmp.nativecoroutines.NativeSuspend
import com.rickclephas.kmp.nativecoroutines.nativeSuspend
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.impl.ProxyFilter
import uk.co.sentinelweb.cuer.app.impl.Utils
import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.usecase.AddBrowsePlaylistUsecase
import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class OrchestratorFactory : KoinComponent {

    val playlistOrchestrator: PlaylistOrchestrator by inject()
    val playlistItemOrchestrator: PlaylistItemOrchestrator by inject()
    val mediaOrchestrator: MediaOrchestrator by inject()
    val databaseInitializer: DatabaseInitializer by inject()
    val proxyFilter: ProxyFilter by inject()
    val utils: Utils by inject()
    private val addBrowsePlaylistUsecase: AddBrowsePlaylistUsecase by inject() // todo UsecaseFactory

    fun addBrowsePlaylistUsecaseExecute(category: CategoryDomain, parentId: OrchestratorContract.Identifier<GUID>?): NativeSuspend<PlaylistDomain?> =
        nativeSuspend {
            addBrowsePlaylistUsecase
                .execute(category, parentId)
        }
}