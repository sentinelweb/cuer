package uk.co.sentinelweb.cuer.app.ui.upcoming

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.ui.upcoming.UpcomingContract.Presenter
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.providers.ignoreJob
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class UpcomingPresenter(
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val coroutines: CoroutineContextProvider,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper,
) : Presenter {

    init {
        log.tag(this)
    }

    override fun checkForUpcomingEpisodes(withinFutureMs: Int) = coroutines.ioScope.launch {
        playlistItemOrchestrator
            .loadList(
                OrchestratorContract.Filter.LiveUpcomingMediaFilter(100),
                OrchestratorContract.Source.LOCAL.deepOptions()
            )


    }.ignoreJob()
}