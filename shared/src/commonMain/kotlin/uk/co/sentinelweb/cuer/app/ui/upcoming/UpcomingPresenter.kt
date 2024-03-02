package uk.co.sentinelweb.cuer.app.ui.upcoming

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.LiveUpcomingMediaFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.ui.upcoming.UpcomingContract.Companion.UPCOMING_LIMIT_MINS
import uk.co.sentinelweb.cuer.app.ui.upcoming.UpcomingContract.Presenter
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider.Companion.toInstant
import uk.co.sentinelweb.cuer.core.providers.ignoreJob
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import kotlin.time.Duration.Companion.minutes


class UpcomingPresenter(
    private val view: UpcomingContract.View,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator, // todo named injection
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
        log.d("now: $now, limit: $limit, diff: ${(limit - now).inWholeMinutes}")
        playlistItemOrchestrator
            .loadList(LiveUpcomingMediaFilter(100), LOCAL.deepOptions())
            .filter { it.media.broadcastDate?.toInstant()?.let { limit > it && now < it } ?: false }
            .forEach { view.showNotification(it) }
    }.ignoreJob()
}