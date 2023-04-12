package uk.co.sentinelweb.cuer.app.service.update

import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.LiveUpcomingMediaFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.usecase.MediaUpdateFromPlatformUseCase
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.providers.ignoreJob
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class UpdateServiceController(
    private val service: UpdateServiceContract.Service,
    private val mediaUpdateFromPlatformUseCase: MediaUpdateFromPlatformUseCase,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val mediaOrchestrator: MediaOrchestrator,
    private val coroutines: CoroutineContextProvider,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper,
) : UpdateServiceContract.Controller {

    init {
        log.tag(this)
    }

    override fun initialise() {

    }

    override fun update() = coroutines.ioScope.launch {
        try { // fixme since broadcast date isn't saved need to update every run - no need to do update after broadcast date is saved
            playlistItemOrchestrator
                .loadList(LiveUpcomingMediaFilter(50), LOCAL.deepOptions())
                .map { it.media }
                .let { mediaUpdateFromPlatformUseCase.updateMediaList(it) }
                .apply { mediaOrchestrator.save(this, LOCAL.flatOptions()) }
                .filter {
                    it.broadcastDate?.let {
                        it.toInstant(TimeZone.UTC).toEpochMilliseconds() > timeProvider.currentTimeMillis() - 60 * 60 * 1000
                    } ?: false
                }
                .takeIf { it.size > 0 }
                ?.apply { service.notify(this) }
        } catch (e: OrchestratorContract.NetException) {
            log.e(e.message ?: "NetException(no message)", e)
        }
    }.ignoreJob()

    override fun destroy() {

    }

    override fun handleAction(action: String?) {
        update()
    }
}
