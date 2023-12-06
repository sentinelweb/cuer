package uk.co.sentinelweb.cuer.app.ui.upcoming

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.GUID

interface UpcomingContract {

    interface Presenter {
        fun checkForUpcomingEpisodes(withinFutureMs: Int)
    }

    interface View {
        fun showNotification()
    }

    data class Model(
        val items: List<Item>
    ) {
        data class Item(
            val episodeId: OrchestratorContract.Identifier<GUID>, // playlist Item
            val episodeTitle: String,
            val startTime: String,
            val startsIn: String
        )
    }

    companion object {
        const val UPCOMING_LIMIT_MINS = 30
    }
}