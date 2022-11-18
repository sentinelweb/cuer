package uk.co.sentinelweb.cuer.app.factory

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.impl.ProxyFilter
import uk.co.sentinelweb.cuer.app.impl.Utils
import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator

class OrchestratorFactory : KoinComponent {

    val playlistOrchestrator: PlaylistOrchestrator by inject()
    val playlistItemOrchestrator: PlaylistItemOrchestrator by inject()
    val mediaOrchestrator: MediaOrchestrator by inject()
    val databaseInitializer: DatabaseInitializer by inject()
    val proxyFilter: ProxyFilter by inject()
    val utils: Utils by inject()
}