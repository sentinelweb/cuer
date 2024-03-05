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
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class UpdateServiceController(
    private val service: UpdateServiceContract.Service,
    private val mediaUpdateFromPlatformUseCase: MediaUpdateFromPlatformUseCase,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val mediaOrchestrator: MediaOrchestrator,
    private val coroutines: CoroutineContextProvider,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper,
    private val notification: UpdateServiceContract.Notification.External,
    private val connectivityWrapper: ConnectivityWrapper,

) : UpdateServiceContract.Controller {

    init {
        log.tag(this)
    }

    override fun initialise() {

    }

    /**
     * Only checks the LiveUpcomingMediaFilter items at the moment - but eventually this should be update other types
     * maybe eliminate PlaylistUpdateUsecase or integrate it here.
     *
     * fixme ??? since broadcast date isn't saved need to update every run
     *   - no need to do update after broadcast date is saved
     *
     */
    override fun update() = coroutines.ioScope.launch {
        notification.updateNotificationStatus("Updating Live Items")
        if (connectivityWrapper.isConnected()) {
            try {
                playlistItemOrchestrator
                    .loadList(LiveUpcomingMediaFilter(50), LOCAL.deepOptions())
                    .map { it.media }
                    .let { mediaUpdateFromPlatformUseCase.updateMediaList(it) }
                    .apply { mediaOrchestrator.save(this, LOCAL.flatOptions()) }
                    .filter {
                        it.broadcastDate?.let {
                            it.toInstant(TimeZone.UTC)
                                .toEpochMilliseconds() > timeProvider.currentTimeMillis() - 60 * 60 * 1000
                        } ?: false
                    }
                    .takeIf { it.size > 0 }
                    ?.apply { notification.updateNotificationResult(this) }
                    ?: apply { notification.updateNotificationResult(emptyList()) }
            } catch (e: OrchestratorContract.NetException) {
                log.e(e.message ?: "NetException(no message)", e)
            }
        } else {
            notification.updateNotificationError("No internet connection")
        }
        service.stopSelf()
    }.ignoreJob()

    override fun destroy() {
        coroutines.cancel()
    }

    override fun handleAction(action: String?) {
        update()
    }
}
