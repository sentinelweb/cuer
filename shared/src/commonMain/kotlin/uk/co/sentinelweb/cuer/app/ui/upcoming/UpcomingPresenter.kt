package uk.co.sentinelweb.cuer.app.ui.upcoming

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.LiveUpcomingMediaFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.ui.upcoming.UpcomingContract.Companion.UPCOMING_LIMIT_MINS
import uk.co.sentinelweb.cuer.app.ui.upcoming.UpcomingContract.Presenter
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider.Companion.toInstant
import uk.co.sentinelweb.cuer.core.providers.ignoreJob
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import kotlin.time.Duration.Companion.minutes


class UpcomingPresenter(
    private val view: UpcomingContract.View,
    private val playlistItemOrchestrator: OrchestratorContract<PlaylistItemDomain>,
    private val mediaOrchestrator: OrchestratorContract<MediaDomain>,
    private val coroutines: CoroutineContextProvider,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper,
) : Presenter {

    init {
        log.tag(this)
    }

    override fun checkForUpcomingEpisodes(withinFutureMins: Int) = coroutines.ioScope.launch {
        log.d("checkForUpcomingEpisodes: $withinFutureMins mins")
        val now = timeProvider.instant()
        val limit = now.plus(UPCOMING_LIMIT_MINS.minutes)
        playlistItemOrchestrator
            .loadList(LiveUpcomingMediaFilter(100), LOCAL.deepOptions())
            .let { loadList ->
                loadList
                    .filter { it.media.broadcastDate?.toInstant()?.let { limit > it && now < it } ?: false }
//            .apply {forEach {  log.d("now items: ${it.media.title} ${it.media.broadcastDate?.let{(it.toInstant()-now).inWholeMinutes}}") }}
                    .forEach { view.showNotification(it) }

                // mark any items that are later
                loadList
                    .filter { it.media.broadcastDate?.toInstant()?.let { now > it } ?: false }
                    .map { it.media.copy(isLiveBroadcastUpcoming = false, isLiveBroadcast = false) }
                    .apply { mediaOrchestrator.save(this, LOCAL.flatOptions()) }
            }
    }.ignoreJob()
}